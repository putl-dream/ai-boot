# 权限管理、身份校验和异常处理实现说明

## 概述

已成功实现了基于JWT的身份认证、基于角色和工具的权限管理、以及全局异常处理功能。

## 实现的功能模块

### 1. 实体类 (Entity)

新增了以下实体类：
- `Conversation` - 会话表
- `Message` - 消息表
- `Model` - 模型表
- `Role` - 角色表
- `RoleModel` - 角色模型关联表
- `RoleTool` - 角色工具关联表
- `SysPrompt` - 系统提示词表
- `Tool` - 工具表
- `User` - 用户表
- `UserRole` - 用户角色关联表
- `UserTool` - 用户工具关联表

路径：`src/main/java/fun/aiboot/entity/`

### 2. Mapper接口

为所有新增实体类创建了MyBatis Plus的Mapper接口：
- `ConversationMapper`
- `MessageMapper`
- `ModelMapper`
- `RoleMapper`
- `RoleModelMapper`
- `RoleToolMapper`
- `SysPromptMapper`
- `ToolMapper`
- `UserMapper`
- `UserRoleMapper`
- `UserToolMapper`

路径：`src/main/java/fun/aiboot/mapper/`

### 3. 全局异常处理

#### 3.1 统一响应结果类
`Result<T>` - 统一的API响应格式

```java
// 成功响应
Result.success(data)
Result.success("消息", data)

// 失败响应
Result.error("错误消息")
Result.error(500, "错误消息")
```

路径：`src/main/java/fun/aiboot/common/Result.java`

#### 3.2 自定义异常类
- `BusinessException` - 业务异常（默认500）
- `AuthenticationException` - 认证异常（401）
- `AuthorizationException` - 授权异常（403）

路径：`src/main/java/fun/aiboot/exception/`

#### 3.3 全局异常处理器
`GlobalExceptionHandler` - 使用`@RestControllerAdvice`实现全局异常捕获

支持的异常类型：
- 业务异常
- 认证/授权异常
- JWT相关异常
- 参数校验异常
- SQL约束异常
- 其他系统异常

路径：`src/main/java/fun/aiboot/exception/GlobalExceptionHandler.java`

### 4. JWT身份认证

#### 4.1 用户上下文
`UserContext` - 使用ThreadLocal存储当前登录用户信息

```java
String userId = UserContext.getUserId();
String username = UserContext.getUsername();
```

路径：`src/main/java/fun/aiboot/context/UserContext.java`

#### 4.2 JWT认证拦截器
`JwtAuthenticationInterceptor` - 拦截所有请求并验证JWT token

特性：
- 从请求头`Authorization`中提取token
- 支持`Bearer`前缀
- 解析token并将用户信息存入ThreadLocal
- 请求完成后自动清理上下文

路径：`src/main/java/fun/aiboot/interceptor/JwtAuthenticationInterceptor.java`

#### 4.3 Web配置
`WebMvcConfig` - 配置拦截器和CORS

白名单（不需要认证）：
- `/user/login` - 登录接口
- `/user/register` - 注册接口
- `/ws/**` - WebSocket连接
- `/error` - 错误页面
- `/actuator/**` - 监控端点

路径：`src/main/java/fun/aiboot/config/WebMvcConfig.java`

### 5. 权限管理

#### 5.1 权限注解

**@RequireRole** - 角色权限校验注解

```java
// 需要拥有admin角色
@RequireRole("admin")

// 需要拥有admin或vip角色之一
@RequireRole({"admin", "vip"})

// 需要同时拥有admin和vip角色
@RequireRole(value = {"admin", "vip"}, requireAll = true)
```

**@RequireTool** - 工具权限校验注解

```java
// 需要拥有image-generator工具权限
@RequireTool("image-generator")

// 需要拥有指定的多个工具权限
@RequireTool(value = {"advanced-search", "data-export"}, requireAll = true)
```

路径：`src/main/java/fun/aiboot/annotation/`

#### 5.2 权限服务
`PermissionService` - 权限校验核心服务

功能：
- 检查用户是否拥有指定角色
- 检查用户是否拥有指定工具权限
- 获取用户的所有角色
- 获取用户的所有工具（包括角色继承的工具）

路径：`src/main/java/fun/aiboot/service/PermissionService.java`

#### 5.3 权限校验切面
`PermissionAspect` - AOP切面，自动拦截带有权限注解的方法

路径：`src/main/java/fun/aiboot/aspect/PermissionAspect.java`

### 6. 密码加密

#### 6.1 密码编码器配置
使用BCrypt算法加密密码

路径：`src/main/java/fun/aiboot/config/PasswordEncoderConfig.java`

#### 6.2 用户服务增强
`UserServiceImpl` - 已增强的用户服务

改进点：
- 注册时自动加密密码
- 登录时使用BCrypt验证密码
- 实现了密码修改功能
- 实现了密码重置功能
- 检查用户名重复
- 统一的异常处理

路径：`src/main/java/fun/aiboot/service/impl/UserServiceImpl.java`

### 6.3 新增服务类

项目新增了以下服务类来支持完整的权限管理和业务逻辑：

- `ConversationService` - 会话管理服务
- `MessageService` - 消息管理服务
- `ModelService` - 模型管理服务
- `RoleService` - 角色管理服务
- `RoleModelService` - 角色模型关联服务
- `RoleToolService` - 角色工具关联服务
- `SysPromptService` - 系统提示词服务
- `ToolService` - 工具管理服务
- `UserRoleService` - 用户角色关联服务
- `UserToolService` - 用户工具关联服务

路径：`src/main/java/fun/aiboot/service/` 和 `src/main/java/fun/aiboot/service/impl/`

### 7. 示例Controller

`UserController` - 演示所有功能的控制器

接口列表：
- `POST /user/login` - 用户登录
- `POST /user/register` - 用户注册
- `GET /user/info` - 获取当前用户信息（需要认证）
- `POST /user/password` - 修改密码（需要认证）
- `POST /user/forget-password` - 忘记密码
- `GET /user/admin` - 管理员接口（需要admin角色）
- `GET /user/tool-test` - 工具权限测试（需要image-generator工具）
- `GET /user/advanced` - 高级功能（需要特定角色和工具权限）

路径：`src/main/java/fun/aiboot/controller/UserController.java`

## 使用示例

### 1. 用户注册

```bash
curl -X POST http://localhost:8080/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com"
  }'
```

响应：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

### 2. 用户登录

```bash
curl -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

响应：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 3. 访问需要认证的接口

```bash
curl -X GET http://localhost:8080/user/info \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 4. 访问需要权限的接口

```bash
# 需要admin角色
curl -X GET http://localhost:8080/user/admin \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

如果没有权限，将返回：
```json
{
  "code": 403,
  "message": "权限不足，需要拥有以下任一角色：[admin]",
  "data": null
}
```

## 架构设计

### 请求流程

```
客户端请求
    ↓
JWT认证拦截器（验证token）
    ↓
权限校验切面（检查角色/工具权限）
    ↓
Controller处理
    ↓
Service业务逻辑
    ↓
统一响应/异常处理
    ↓
返回给客户端
```

### 权限设计

项目采用RBAC（基于角色的访问控制）+ 工具权限的混合模型：

1. **用户 ← 角色**：一个用户可以拥有多个角色
2. **角色 ← 工具**：一个角色可以关联多个工具
3. **用户 ← 工具**：用户也可以直接拥有工具权限

权限继承关系：
- 用户的最终工具权限 = 用户直接拥有的工具 + 用户所有角色拥有的工具

## 依赖说明

已添加以下依赖：
- `spring-boot-starter-aop` - AOP支持
- `spring-security-crypto` - 密码加密

路径：`pom.xml`

## 配置说明

### JWT配置
JWT密钥和过期时间在`JwtUtil`中配置：
- 过期时间：12小时
- 建议将密钥移至配置文件

### 拦截器白名单
在`WebMvcConfig`中配置不需要认证的接口

## 安全建议

1. **JWT密钥**：将硬编码的密钥改为从配置文件读取
2. **Token刷新**：建议实现token刷新机制
3. **密码策略**：添加密码强度校验
4. **登录限制**：添加登录失败次数限制
5. **日志审计**：记录重要操作的审计日志

## 注意事项

1. 所有需要认证的接口都需要在请求头中携带`Authorization: Bearer {token}`
2. 权限注解可以用在方法或类上
3. 异常会被全局异常处理器捕获并转换为统一的响应格式
4. 密码使用BCrypt加密，不可逆
5. 用户上下文在每次请求结束后会自动清理

## 文件清单

### 新增文件
- 实体类：7个
- Mapper：7个
- 异常类：4个
- 配置类：3个
- 服务类：6个
- 拦截器：1个
- 切面：1个
- 注解：2个
- 控制器：1个
- 工具类：1个

### 修改文件
- `pom.xml` - 添加依赖
- `UserServiceImpl.java` - 增强用户服务

总计新增约32个文件，修改2个文件。
