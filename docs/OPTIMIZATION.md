# 项目优化建议

> 分析日期：2026-04-24
> 项目技术栈：Spring Boot 3.2.0 + MyBatis + Redis + Spring Security + JWT

---

## 一、安全问题（高优先级）

### 1.1 密钥和密码硬编码 [P0]

**问题描述：** 多处敏感配置直接写在配置文件中，存在泄露风险。

| 配置项 | 位置 | 当前值 | 风险 |
|--------|------|--------|------|
| JWT密钥 | `application-jwt.yml` | 占位符文本 | 攻击者可伪造任意token |
| 数据库密码 | `application-datasource.yml` | `123456` | 数据库被暴力破解 |
| Redis | `application-redis.yml` | 无密码 | 本地Redis被未授权访问 |

**修复建议：**
```yaml
# 使用环境变量
jwt:
  secret: ${JWT_SECRET}
spring:
  datasource:
    password: ${SPRING_DATASOURCE_PASSWORD}
```

---

### 1.2 敏感接口未认证 [P1]

**问题描述：** 以下接口配置为允许匿名访问，但可能泄露用户信息：

```yaml
# application-security.yml
allowed-paths:
  - /api/profile      # 返回默认用户"谢沁桐"的信息
  - /api/visit/**     # 访问统计接口
```

**修复建议：** 将 `/api/profile` 和 `/api/visit/**` 从允许列表移除，添加适当认证。

---

### 1.3 密码强度不足 [P1]

**问题描述：** 注册时仅验证长度（6-100位），未验证复杂度。

```java
// RegisterRequest.java
@Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
```

**修复建议：** 添加复杂度验证
```java
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
         message = "密码必须包含大小写字母、数字和特殊字符")
private String password;
```

---

### 1.4 异常信息泄露 [P2]

**问题描述：** 生产环境将详细错误信息返回给客户端。

```java
// GlobalExceptionHandler.java
return ResponseEntity
    .status(HttpStatus.INTERNAL_SERVER_ERROR)
    .body(ApiResponse.error(500, "服务器内部错误：" + e.getMessage()));
```

**修复建议：**
```java
String errorId = UUID.randomUUID().toString();
log.error("Error ID: {}, message: {}", errorId, e.getMessage());
return ResponseEntity
    .status(HttpStatus.INTERNAL_SERVER_ERROR)
    .body(ApiResponse.error(500, "服务器内部错误，请联系管理员并提供错误ID: " + errorId));
```

---

## 二、代码质量问题

### 2.1 JWT过滤器异常处理过于宽泛

**位置：** `JwtAuthenticationFilter.java:64-66`

```java
} catch (Exception e) {
    log.error("JWT过滤器处理异常: {}", e.getMessage());
}
// 异常被捕获后静默处理，请求继续进行
```

**问题：** 任何JWT解析异常都被吞掉，可能导致安全隐患。

**建议：** 区分异常类型，对恶意请求进行记录和拒绝。

---

### 2.2 注册时邮箱唯一性未验证

**问题描述：** 数据库有 `idx_email` 索引，但注册接口未检查邮箱是否已被使用。

---

### 2.3 UserMapper配置冗余

**问题描述：** Mapper接口使用 `@Mapper` 注解，但同时存在 `UserMapper.xml` 并配置了 `mybatis.mapper-locations`。

**建议：** 保留一种方式即可，推荐使用XML配置SQL。

---

## 三、配置问题

### 3.1 Docker Compose配置错误

**位置：** `docker-compose.yml:24-25`

```yaml
- ./src/main/resources/schema.sql:/docker-entrypoint-initdb.d/init.sql
```

**问题：** 使用 `schema.sql` 但项目实际只有 `init.sql`。

---

### 3.2 配置命名不一致

**问题描述：** `application-security.yml` 中使用 `csrf.enabled`（下划线），但 `SecurityConfig` 中使用 `csrfEnabled`（驼峰）。

---

### 3.3 Redis连接池配置未启用

**位置：** `application-redis.yml` 定义了Lettuce连接池配置，但主配置未引入。

---

## 四、可维护性问题

### 4.1 缺少单元测试

**问题描述：** 项目没有任何测试代码。

---

### 4.2 代码重复

**位置：** `JwtAuthenticationFilter.java:26-28`

```java
public void setJwtUtil(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
}
```

**问题：** 应使用构造器注入（项目其他地方已使用 `@RequiredArgsConstructor`）。

---

### 4.3 前端token存储不安全

**位置：** `script.js`

```javascript
localStorage.setItem('token', data.token);
```

**问题：** localStorage中的token可被XSS攻击窃取。

**建议：** 使用 `HttpOnly` Cookie存储JWT。

---

### 4.4 配置文件注释混乱

**问题描述：** 配置文件中有大量注释掉的本地配置和容器配置切换代码。

---

## 五、性能优化

### 5.1 Redis过期时间设置优化

**位置：** `VisitServiceImpl.java:28`

```java
redisTemplate.expire(key, 7, TimeUnit.DAYS);
```

**问题：** 每次访问都设置过期时间是不必要的。

**建议：** 在key创建时设置过期时间即可。

---

### 5.2 HikariCP连接池可调整

**当前值：** `maximum-pool-size: 10`, `minimum-idle: 5`

**建议：** 根据实际负载调整，高并发场景可适当增大。

---

## 六、优化优先级汇总

| 优先级 | 问题 | 类型 |
|--------|------|------|
| **P0** | JWT密钥硬编码 | 安全 |
| **P0** | 数据库密码硬编码 | 安全 |
| **P1** | 敏感接口未认证 | 安全 |
| **P1** | Redis无密码 | 安全 |
| **P1** | 密码强度不足 | 安全 |
| **P2** | 异常信息泄露 | 安全 |
| **P2** | Docker配置错误 | 配置 |
| **P2** | 缺少单元测试 | 可维护性 |
| **P3** | JWT过滤器异常处理 | 代码质量 |
| **P3** | 前端token存储 | 安全 |
| **P3** | 代码重复/不一致 | 代码质量 |

---

## 七、总体评分

| 维度 | 评分 | 说明 |
|------|------|------|
| 技术选型 | 7/10 | 栈选型合理 |
| 代码质量 | 6/10 | 基础功能完整 |
| 安全 | 4/10 | 存在多处高危安全风险 |
| 可维护性 | 6/10 | 结构清晰，但缺少文档和测试 |
| 部署 | 6/10 | Docker支持，但配置存在问题 |

**建议优先修复 P0 和 P1 级安全问题后再进行生产部署。**
