# 项目分析文档

## 一、技术栈

| 类型 | 技术 |
|------|------|
| 框架 | Spring Boot 3.2.0 |
| Java版本 | Java 21 |
| 数据库 | MySQL 8.0 (JDBC) |
| 缓存 | Redis 7.0 |
| 安全 | Spring Security + JWT |
| 构建工具 | Maven |
| 其他 | Lombok, Validation |

## 二、项目结构

```
src/main/java/org/example/
├── MyApplication.java           # 启动类
├── config/                     # 配置类
│   ├── JwtAuthenticationFilter.java  # JWT认证过滤器
│   ├── JwtConfig.java          # JWT配置
│   ├── RedisConfig.java        # Redis配置
│   └── SecurityConfig.java     # 安全配置
├── controller/                 # 控制器
│   ├── AuthController.java     # 认证接口
│   └── ProfileController.java  # 用户资料接口
├── dao/                        # 数据访问层
│   ├── UserDao.java
│   └── impl/UserDaoImpl.java
├── dto/                        # 数据传输对象
│   ├── ApiResponse.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   └── RegisterRequest.java
├── entity/                     # 实体类
│   └── User.java
├── exception/                  # 异常处理
│   ├── BusinessException.java
│   └── GlobalExceptionHandler.java
├── model/                      # 模型
│   └── Profile.java
├── service/                    # 业务逻辑
│   ├── AuthService.java
│   ├── impl/
│   │   ├── AuthServiceImpl.java
│   │   └── CustomUserDetailsService.java
└── util/                       # 工具类
    └── JwtUtil.java
```

## 三、功能模块

### 1. 认证系统
- 用户登录/注册
- JWT Token生成与验证
- Spring Security 集成

### 2. 用户管理
- 用户数据持久化 (MySQL)
- 用户资料管理

### 3. 缓存
- Redis 集成
- 用于会话/Token缓存

## 四、配置文件

| 文件 | 用途 |
|------|------|
| application-datasource.yml | MySQL数据库配置 |
| application-redis.yml | Redis配置 |
| application-jwt.yml | JWT配置 |
| application-security.yml | 安全配置 |
| application-logging.yml | 日志配置 |
| application-server.yml | 服务器配置 |
| application-app.yml | 应用配置 |

## 五、部署架构

### Docker Compose
- **app**: Spring Boot 应用 (端口 8080)
- **mysql**: MySQL 8.0 容器
- **redis**: Redis 7.0 容器

### 环境变量
- `SPRING_DATASOURCE_URL`: 数据库连接地址
- `SPRING_DATASOURCE_USERNAME`: 数据库用户名
- `SPRING_DATASOURCE_PASSWORD`: 数据库密码
- `SPRING_REDIS_HOST`: Redis主机
- `SPRING_REDIS_PORT`: Redis端口

## 六、已知问题

根据Git提交记录，项目存在以下问题：
- Web页面已可访问
- MySQL和Redis连接未成功

---

*文档创建日期: 2026-04-09*
