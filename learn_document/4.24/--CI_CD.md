# GitHub Actions CI/CD 部署指南

> 本文档描述如何使用 GitHub Actions 实现项目的持续集成和持续部署。

---

## 一、整体架构

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Code      │───▶│   Build     │───▶│   Test      │
│   Push      │    │   JAR       │    │   Unit      │
└─────────────┘    └─────────────┘    └─────────────┘
                                              │
                   ┌──────────────────────────┘
                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Deploy    │◀───│   Push      │◀───│   Build     │
│   to Server │    │   Image     │    │   Docker    │
└─────────────┘    └─────────────┘    └─────────────┘
```

---

## 二、GitHub Actions 配置

### 2.1 创建工作流目录

```bash
mkdir -p .github/workflows
```

### 2.2 CI 工作流 (ci.yml)

负责代码构建和单元测试。

```bash
cat > .github/workflows/ci.yml << 'EOF'
name: CI - Build and Test

on:
  push:
    branches: [ master, develop ]
  pull_request:
    branches: [ master, develop ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Build with Maven
        run: mvn clean package -DskipTests

      - name: Run unit tests
        run: mvn test

      - name: Upload build artifacts
        uses: actions/upload-artifact@v4
        with:
          name: jar-file
          path: target/my_web_1-1.0-SNAPSHOT.jar
EOF
```

### 2.3 CD 工作流 (cd.yml)

负责 Docker 镜像构建、推送和服务器部署。

```bash
cat > .github/workflows/cd.yml << 'EOF'
name: CD - Build, Push and Deploy

on:
  workflow_run:
    workflows: ["CI - Build and Test"]
    types: [completed]
    branches: [master]

jobs:
  build-and-push:
    if: github.event.workflow_run.conclusion == 'success'
    runs-on: ubuntu-latest
    outputs:
      image-tag: ${{ steps.meta.outputs.tags }}
      image-digest: ${{ steps.build.outputs.digest }}

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Get Maven dependencies
        run: mvn dependency:go-offline

      - name: Build JAR
        run: mvn clean package -DskipTests

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ secrets.DOCKERHUB_USERNAME }}/my-web-app
          tags: |
            type=sha,prefix=
            type=ref,event=branch
            type=semver,pattern={{version}}

      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

  deploy:
    needs: build-and-push
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to server via SSH
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          script: |
            cd /opt/my-web-app
            docker-compose pull
            docker-compose up -d
            docker image prune -f
EOF
```

---

## 三、必要配置

### 3.1 GitHub Secrets

在 GitHub 仓库的 `Settings > Secrets and variables > Actions` 中添加：

| Secret 名称 | 说明 | 示例值 |
|-------------|------|--------|
| `DOCKERHUB_USERNAME` | Docker Hub 用户名 | `myuser` |
| `DOCKERHUB_TOKEN` | Docker Hub Access Token | `dckr_pat_xxx` |
| `SERVER_HOST` | 服务器 IP 或域名 | `123.45.67.89` |
| `SERVER_USER` | 服务器用户名 | `ubuntu` |
| `SERVER_SSH_KEY` | 服务器 SSH 私钥 | `-----BEGIN OPENSSH PRIVATE KEY-----...` |

### 3.2 Docker Hub Access Token 创建

1. 登录 [Docker Hub](https://hub.docker.com/)
2. 进入 `Account Settings > Security > Access Tokens`
3. 点击 "Generate Access Token"
4. 复制生成的 Token 并保存到 GitHub Secrets

### 3.3 服务器 SSH 密钥配置

**本地生成密钥对：**
```bash
ssh-keygen -t ed25519 -C "github-actions-deploy"
```

**将公钥添加到服务器：**
```bash
ssh-copy-id -i ~/.ssh/id_ed25519.pub user@server
```

**将私钥添加到 GitHub Secrets (`SERVER_SSH_KEY`)：**
```bash
cat ~/.ssh/id_ed25519
```

---

## 四、服务器部署配置

### 4.1 服务器目录结构

```bash
/opt/my-web-app/
├── docker-compose.yml
├── .env
└── app/
    └── (Dockerfile if needed)
```

### 4.2 生产环境 docker-compose.yml

```bash
cat > /opt/my-web-app/docker-compose.yml << 'EOF'
version: '3.8'

services:
  app:
    image: ${DOCKERHUB_USERNAME}/my-web-app:${IMAGE_TAG:-latest}
    container_name: my-web-app
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/my_web_db?useSSL=false&serverTimezone=Asia/Shanghai
      - SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
    restart: unless-stopped

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=my_web_db
    volumes:
      - mysql-data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    restart: unless-stopped

  redis:
    image: redis:7.0-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis-data:/data
    restart: unless-stopped

volumes:
  mysql-data:
  redis-data:
EOF
```

### 4.3 服务器环境变量文件

```bash
cat > /opt/my-web-app/.env << 'EOF'
# Docker Hub
DOCKERHUB_USERNAME=myuser

# 数据库
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_secure_password
MYSQL_ROOT_PASSWORD=your_secure_password

# JWT
JWT_SECRET=your-256-bit-secret-key-for-jwt-signing-must-be-at-least-32-characters-long

# Redis
REDIS_PASSWORD=your_redis_password

# 镜像标签（可选，默认latest）
IMAGE_TAG=latest
EOF
```

### 4.4 服务器初始化脚本

```bash
cat > /opt/my-web-app/init.sh << 'EOF'
#!/bin/bash
set -e

# 创建目录
sudo mkdir -p /opt/my-web-app
cd /opt/my-web-app

# 拉取最新代码/配置（如果使用git）
# git pull origin master

# 启动服务
docker-compose pull
docker-compose up -d

# 清理旧镜像
docker image prune -f

echo "Deployment completed!"
EOF
chmod +x /opt/my-web-app/init.sh
```

---

## 五、优化 Dockerfile（多阶段构建）

更新 Dockerfile 使用多阶段构建，减小镜像体积：

```bash
cat > Dockerfile << 'EOF'
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 添加非root用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/target/my_web_1-1.0-SNAPSHOT.jar app.jar

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Xms256m", "-Xmx512m", "app.jar"]
EOF
```

---

## 六、GitHub Container Registry（可选）

如果使用 GitHub Container Registry 替代 Docker Hub：

```yaml
# cd.yml 中的镜像构建部分
- name: Login to GitHub Container Registry
  uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}

# images 改为
images: ghcr.io/${{ github.repository_owner }}/my-web-app
```

---

## 七、工作流程说明

### 7.1 触发条件

| 事件 | CI | CD |
|------|----|----|
| push to master | ✓ | ✓ (CI成功后) |
| push to develop | ✓ | ✗ |
| pull request | ✓ | ✗ |

### 7.2 完整流程

```
1. 开发者推送代码到 master
         │
         ▼
2. GitHub Actions 自动触发 CI workflow
         │
         ├── mvn clean package    (构建)
         │
         ├── mvn test             (单元测试)
         │
         └── upload artifact       (上传JAR)
         │
         ▼ (CI 成功)
3. CD workflow 自动触发
         │
         ├── docker buildx        (构建镜像)
         │
         ├── docker push          (推送到仓库)
         │
         └── ssh deploy           (部署到服务器)
```

---

## 八、注意事项

### 8.1 安全建议

1. **不要提交敏感信息到 GitHub**
   - `.env` 文件已在 `.gitignore` 中
   - 使用 GitHub Secrets 存储所有密钥

2. **服务器 SSH 密钥**
   - 使用专用部署密钥，不要使用个人密钥
   - 定期轮换

3. **Docker 镜像**
   - 使用轻量级基础镜像 (`eclipse-temurin:21-jre-alpine`)
   - 不要在镜像中存储密钥

### 8.2 故障排除

**部署失败检查清单：**
1. GitHub Secrets 是否正确配置
2. 服务器 SSH 连接是否正常
3. Docker Hub 镜像仓库是否存在
4. 服务器磁盘空间是否充足
5. docker-compose.yml 配置是否正确

**常用命令：**
```bash
# 服务器上手动查看日志
docker-compose logs -f app

# 服务器上手动重启
docker-compose restart app

# 查看容器状态
docker-compose ps
```

---

## 九、扩展功能

### 9.1 添加单元测试覆盖检查

```yaml
- name: Generate coverage report
  run: mvn jacoco:report

- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v4
  with:
    files: target/site/jacoco/jacoco.xml
```

### 9.2 添加 SonarQube 静态分析

```yaml
- name: SonarQube Scan
  uses: sonarsource/sonarqube-scan-action@v4
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
    SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
```

### 9.3 添加 Slack/钉钉通知

```yaml
- name: Notify on failure
  if: failure()
  uses: slackapi/slack-github-action@v1
  with:
    payload: |
      {
        "text": "Deployment failed: ${{ github.run_webho }}",
        "blocks": [...]
      }
```
