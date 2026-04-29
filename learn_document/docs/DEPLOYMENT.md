# Ubuntu 轻量应用服务器部署指南

本文档描述如何将 `my_web_1` 项目部署到仅安装了 Docker 的 Ubuntu 轻量应用服务器上。

---

## 环境概览

| 项目 | 说明 |
|------|------|
| 服务器系统 | Ubuntu (轻量应用服务器) |
| 已安装软件 | Docker |
| 应用框架 | Spring Boot 3.2.0 (Java 21) |
| 构建工具 | Maven |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7.0 |
| 消息队列 | RabbitMQ 3.12 |

---

## 部署方案选择

本项目支持两种部署方式：

| 方式 | 说明 | 适用场景 |
|------|------|----------|
| **方式一：docker-compose 一键部署** | 使用项目自带的 docker-compose.yml，所有服务一键启动 | 快速部署，推荐使用 |
| **方式二：独立容器部署** | Nginx + Java 应用 + MySQL + Redis + RabbitMQ 分开部署 | 需要更灵活配置时 |

**推荐使用方式一**，本文档以此为主。

---

## 部署前准备

### 1. 检查服务器环境

通过 SSH 连接到服务器后，执行以下命令确认 Docker 已安装：

```bash
docker --version
docker-compose --version
# 或
docker compose version
```

### 2. 创建项目目录

```bash
# 创建项目目录
mkdir -p /opt/my_web_1
cd /opt/my_web_1
```

---

## 方式一：docker-compose 一键部署（推荐）

### 步骤 1：上传项目文件

将本地项目文件上传到服务器。可使用以下任一方式：

**方式 A：使用 scp（命令行）**

```bash
# 在本地执行，将整个项目上传到服务器
scp -r /path/to/my_web_1/* root@你的服务器IP:/opt/my_web_1/
```

**方式 B：使用 rsync（支持断点续传）**

```bash
rsync -avz --progress /path/to/my_web_1/* root@你的服务器IP:/opt/my_web_1/
```

### 步骤 2：配置环境变量

创建 `.env` 文件配置生产环境参数：

```bash
cd /opt/my_web_1
nano .env
```

填入以下内容（根据实际情况修改）：

```env
# 数据库配置
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=你的MySQL密码

# JWT 配置（必须至少32字符）
JWT_SECRET=你的JWT密钥至少32位字符长度

# Redis 配置
REDIS_PASSWORD=你的Redis密码

# RabbitMQ 配置
RABBITMQ_HOST=rabbitmq
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=你的RabbitMQ密码

# 邮件配置
MAIL_HOST=smtp.qq.com
MAIL_PORT=587
MAIL_USERNAME=your-email@qq.com
MAIL_PASSWORD=你的邮箱授权码
```

### 步骤 3：修改 docker-compose.yml（如需要）

编辑 `docker-compose.yml`，调整端口映射：

```bash
nano docker-compose.yml
```

```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"        # 可改为其他端口，如 "80:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/my_web_db?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      - SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - RABBITMQ_HOST=${RABBITMQ_HOST}
      - RABBITMQ_USERNAME=${RABBITMQ_USERNAME}
      - RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD}
      - MAIL_HOST=${MAIL_HOST}
      - MAIL_PORT=${MAIL_PORT}
      - MAIL_USERNAME=${MAIL_USERNAME}
      - MAIL_PASSWORD=${MAIL_PASSWORD}
    depends_on:
      - mysql
      - redis
      - rabbitmq

  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
      - MYSQL_DATABASE=my_web_db
    volumes:
      - mysql_data:/var/lib/mysql
      - ./src/main/resources/init.sql:/docker-entrypoint-initdb.d/init.sql
    command: --default-authentication-plugin=mysql_native_password --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

  redis:
    image: redis:7.0
    ports:
      - "6379:6379"
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data

  rabbitmq:
    image: rabbitmq:3.12-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      - RABBITMQ_DEFAULT_USER=${RABBITMQ_USERNAME}
      - RABBITMQ_DEFAULT_PASS=${RABBITMQ_PASSWORD}
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq

volumes:
  mysql_data:
  redis_data:
  rabbitmq_data:
```

### 步骤 4：启动服务

```bash
cd /opt/my_web_1

# 构建并启动所有服务（后台运行）
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f app
```

### 步骤 5：验证部署

```bash
# 检查容器是否运行
docker ps

# 测试应用是否启动成功
curl http://localhost:8080/actuator/health

# 访问 MySQL
docker exec -it my_web_1-mysql-1 mysql -u root -p

# 访问 Redis
docker exec -it my_web_1-redis-1 redis-cli -a 你的Redis密码

# 访问 RabbitMQ 管理界面
# 浏览器打开 http://你的服务器IP:15672
```

---

## 方式二：独立容器部署（可选）

如果需要更灵活的配置，可以分别部署各个组件。

### 步骤 1：安装 Docker（如果尚未安装）

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装依赖
sudo apt install -y ca-certificates curl gnupg

# 添加 Docker GPG 密钥
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# 添加 Docker 仓库
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list

# 安装 Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 启动 Docker
sudo systemctl start docker
sudo systemctl enable docker

# 添加当前用户到 docker 组（免 sudo）
sudo usermod -aG docker $USER
```

### 步骤 2：部署 MySQL

```bash
# 创建网络
docker network create my_web_network

# 运行 MySQL
docker run -d \
  --name mysql \
  --network my_web_network \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=你的密码 \
  -e MYSQL_DATABASE=my_web_db \
  -v mysql_data:/var/lib/mysql \
  -v /opt/my_web_1/src/main/resources/init.sql:/docker-entrypoint-initdb.d/init.sql \
  mysql:8.0 \
  --default-authentication-plugin=mysql_native_password \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

### 步骤 3：部署 Redis

```bash
docker run -d \
  --name redis \
  --network my_web_network \
  -p 6379:6379 \
  -e REDIS_PASSWORD=你的密码 \
  -v redis_data:/data \
  redis:7.0 \
  redis-server --requirepass 你的密码
```

### 步骤 4：部署 RabbitMQ

```bash
docker run -d \
  --name rabbitmq \
  --network my_web_network \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=你的密码 \
  -v rabbitmq_data:/var/lib/rabbitmq \
  rabbitmq:3.12-management
```

### 步骤 5：构建并运行 Java 应用

```bash
cd /opt/my_web_1

# 构建镜像
docker build -t my_web_1:latest .

# 运行应用
docker run -d \
  --name app \
  --network my_web_network \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/my_web_db?useSSL=false\&serverTimezone=Asia/Shanghai\&allowPublicKeyRetrieval=true \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=你的密码 \
  -e JWT_SECRET=你的JWT密钥至少32位 \
  -e REDIS_PASSWORD=你的密码 \
  -e RABBITMQ_HOST=rabbitmq \
  -e RABBITMQ_USERNAME=guest \
  -e RABBITMQ_PASSWORD=你的密码 \
  -e MAIL_HOST=smtp.qq.com \
  -e MAIL_PORT=587 \
  -e MAIL_USERNAME=your-email@qq.com \
  -e MAIL_PASSWORD=你的邮箱授权码 \
  my_web_1:latest
```

---

## 配置 Nginx 反向代理（可选）

如需通过域名访问，并启用 HTTPS，推荐使用 Nginx。

### 步骤 1：安装 Nginx

```bash
sudo apt install -y nginx
```

### 步骤 2：配置反向代理

```bash
sudo nano /etc/nginx/sites-available/my_web
```

写入以下内容：

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

启用配置：

```bash
sudo ln -s /etc/nginx/sites-available/my_web /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 步骤 3：申请 SSL 证书（使用 Let's Encrypt）

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

---

## 常用运维命令

### 启动/停止/重启

```bash
# docker-compose 方式
docker-compose start
docker-compose stop
docker-compose restart

# docker 方式
docker start app mysql redis rabbitmq
docker stop app mysql redis rabbitmq
```

### 查看日志

```bash
# docker-compose 方式
docker-compose logs -f
docker-compose logs -f app

# docker 方式
docker logs -f app
docker logs -f mysql
```

### 数据备份

```bash
# 备份 MySQL
docker exec mysql mysqldump -u root -p你的密码 my_web_db > backup.sql

# 备份 Redis
docker exec redis redis-cli -a 你的密码 SAVE
docker cp redis:/data/dump.rdb ./redis_backup.rdb

# 备份 RabbitMQ
docker cp rabbitmq:/var/lib/rabbitmq ./rabbitmq_backup
```

### 清理资源

```bash
# 停止并删除容器
docker-compose down

# 删除容器和数据卷
docker-compose down -v

# 删除未使用的镜像
docker image prune -a
```

### 更新部署

```bash
cd /opt/my_web_1

# 拉取最新代码后重新构建
git pull
docker-compose up -d --build
```

---

## 防火墙配置

```bash
# 查看防火墙状态
sudo ufw status

# 开放必要端口
sudo ufw allow 22    # SSH
sudo ufw allow 80     # HTTP
sudo ufw allow 443    # HTTPS
sudo ufw allow 8080   # Java 应用（如果直接访问）
sudo ufw allow 3306   # MySQL（建议限制来源）
sudo ufw allow 6379   # Redis（建议限制来源）
sudo ufw allow 5672   # RabbitMQ（建议限制来源）
sudo ufw allow 15672  # RabbitMQ 管理界面（建议限制来源）

# 启用防火墙
sudo ufw enable
```

---

## 安全建议

1. **修改默认密码**：立即修改 MySQL、Redis、RabbitMQ 的默认密码
2. **限制端口访问**：数据库和缓存端口不要暴露到公网
3. **使用环境变量**：敏感信息不要写死在配置文件中
4. **定期更新**：定期更新 Docker 镜像和系统补丁
5. **日志监控**：配置日志收集和告警机制
6. **SSL 证书**：生产环境务必启用 HTTPS

---

## 目录结构

部署完成后，项目目录结构如下：

```
/opt/my_web_1/
├── .env                          # 环境变量配置
├── docker-compose.yml            # Docker Compose 配置
├── Dockerfile                    # 应用镜像构建文件
├── src/
│   └── main/
│       └── resources/
│           ├── application*.yml   # 应用配置文件
│           └── init.sql           # 数据库初始化脚本
├── logs/                         # 应用日志目录（如配置）
└── backup/                      # 备份文件目录（手动创建）
```

---

## 常见问题

### Q1: 容器启动失败怎么办？

```bash
# 查看详细日志
docker-compose logs app
docker-compose logs mysql

# 检查端口占用
sudo netstat -tlnp | grep 8080
```

### Q2: 数据库连接失败？

检查以下几点：
- MySQL 是否已完全启动（等待约30秒）
- `SPRING_DATASOURCE_URL` 中的主机名是否正确
- 数据库密码是否正确
- 确认 `init.sql` 已正确执行

### Q3: 内存不足？

Spring Boot 已配置内存限制（`-Xms256m -Xmx512m`）。如果服务器内存较小，可以：
- 减少 Docker 堆内存配置
- 关闭不必要的服务
- 添加 swap 分区

### Q4: 如何查看应用健康状态？

```bash
curl http://localhost:8080/actuator/health
```

---

## 参考链接

- [Docker 官方文档](https://docs.docker.com/)
- [Docker Compose 官方文档](https://docs.docker.com/compose/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MySQL 8.0 Docker 镜像](https://hub.docker.com/_/mysql)
- [Redis Docker 镜像](https://hub.docker.com/_/redis)
- [RabbitMQ Docker 镜像](https://hub.docker.com/_/rabbitmq)
