# Jenkins 安装与配置

## 服务器要求

- Ubuntu 20.04+ / Debian
- 内存建议 2GB+
- Docker 已安装
- Git 已安装

## 1. 安装 Jenkins

### 方式一：Docker 部署（推荐）

```bash
# 创建 Jenkins 数据目录
mkdir -p /opt/jenkins

# 运行 Jenkins 容器
docker run -d \
  --name jenkins \
  -p 8081:8080 \
  -p 50000:50000 \
  -v /opt/jenkins:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts
```

### 方式二：直接安装

```bash
# 安装 Java
apt update
apt install -y openjdk-17-jdk

# 安装 Jenkins
wget -q -O /usr/share/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc]" \
  https://pkg.jenkins.io/debian-stable binary/ | tee /etc/apt/sources.list.d/jenkins.list > /dev/null
apt update
apt install -y jenkins

systemctl start jenkins
systemctl enable jenkins
```

## 2. 初始化 Jenkins

### 获取初始管理员密码

```bash
# Docker 部署
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# 直接安装
cat /var/lib/jenkins/secrets/initialAdminPassword
```

### 访问 Jenkins

打开浏览器访问：`http://你的服务器IP:8081`

按提示完成：
1. 输入初始密码
2. 安装推荐插件
3. 创建管理员账号

## 3. 配置 Jenkins

### 安装必要插件

进入 **Manage Jenkins** → **Manage Plugins** → **Available**

安装以下插件：
- **GitHub Integration** - GitHub 集成
- **Publish Over SSH** - 远程部署
- **Docker Pipeline** - Docker 支持（可选）

### 配置 GitHub Webhook

1. GitHub 仓库 → **Settings** → **Webhooks** → **Add webhook**
2. Payload URL: `http://你的服务器IP:8081/github-webhook/`
3. Content type: `application/json`
4. Events: Just the push event

### 配置服务器 SSH

**方式一：使用 SSH Agent（推荐）**

在 Jenkins 中：
1. **Manage Jenkins** → **Manage Credentials** → **Add Credentials**
2. 选择 **SSH Username with private key**
3. 输入服务器 root 用户和私钥

**方式二：使用 Publish Over SSH 插件**

1. **Manage Jenkins** → **System** → **Publish over SSH**
2. 添加 SSH Server：
   - Name: `server`
   - Host: `101.133.155.157`
   - Username: `root`
   - SSH Key: 选择刚才添加的 credentials

## 4. 创建构建任务

### 新建任务

1. 点击 **New Item**
2. 输入任务名称，选择 **Freestyle project**
3. 配置：

#### Source Code Management

```
Repository URL: git@github.com:moffiescat/my_web_1.git
Credentials: 添加 GitHub SSH Key
Branches to build: */master
```

#### Build Triggers

勾选 **GitHub hook trigger for GITScm polling**

#### Build Steps

**方式一：Execute shell**

```bash
cd /opt/my_web_1
git pull
docker-compose build app
docker-compose up -d app
```

**方式二：Send files or execute commands over SSH**

```bash
cd /opt/my_web_1
git pull origin master
docker-compose build --no-cache app
docker-compose up -d app
docker logs --tail 50 my_web_1-app-1
```

## 5. 本地开发流程

1. 本地修改代码
2. Push 到 GitHub master 分支
3. GitHub 发送 webhook 到 Jenkins
4. Jenkins 自动构建并部署到服务器

## 6. 常见问题

### 构建时找不到 docker 命令

```bash
# Jenkins 容器内安装 docker
docker exec jenkins apt-get update
docker exec jenkins apt-get install -y docker.io
```

### 权限问题

```bash
# 将 jenkins 用户加入 docker 组
docker exec jenkins usermod -aG docker jenkins
# 重启 Jenkins
docker restart jenkins
```

### Maven 构建慢

可以在 Jenkins 容器中配置 Maven 镜像：

```bash
docker exec -it jenkins bash
# 编辑 /var/jenkins_home/hudson.tasks.Maven_MavenInstallation/xxx/tools/hudson.tasks.Maven_MavenInstallation/xxx/config.xml
```

### Git 拉取失败

确保 Jenkins 的 SSH Key 已添加到 GitHub Deploy Keys：
1. GitHub 仓库 → **Settings** → **Deploy Keys**
2. 添加 Jenkins 服务器的公钥

## 7. 验证 CI/CD

```bash
# 手动触发一次构建
curl -u 用户名:API_TOKEN http://服务器IP:8081/job/任务名/build
```

## 8. 安全建议

- 修改 Jenkins 默认端口
- 启用用户注册需要管理员审批
- 定期更新 Jenkins 和插件
- 使用防火墙限制 8081 端口访问

## 9. 卸载 Jenkins

```bash
# Docker 部署
docker stop jenkins
docker rm jenkins
rm -rf /opt/jenkins

# 直接安装
systemctl stop jenkins
apt remove -y jenkins
rm -rf /var/lib/jenkins
rm -rf /var/cache/jenkins
```
