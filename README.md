# AI-Boot

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-blue.svg)](https://docs.spring.io/spring-ai/reference/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

一个基于 Spring Boot 和 Spring AI 构建的智能对话系统框架，提供高度可扩展、模块化的 AI 应用开发基础设施。
集成了 JWT 身份认证、RBAC 权限管理、WebSocket 实时通信和 Function Calling 工具调用等企业级功能。

## 核心特性

- **模块化架构** - 业务与技术分离，模块间通过接口松耦合
- **多模型支持** - 支持阿里云通义千问、OpenAI 等多种 AI 模型
- **WebSocket 通信** - 实时双向通信，支持流式响应
- **工具调用** - 支持 Function Calling，扩展 AI 能力
- **自动消息路由** - 基于类型的智能消息分发
- **流式响应** - 实时传输 AI 响应，提升用户体验
- **JWT 身份认证** - 基于 JWT 的安全身份验证机制
- **RBAC 权限管理** - 基于角色和工具的细粒度权限控制
- **全局异常处理** - 统一的异常处理和响应格式

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+
- MySQL 8.0+
- 通义千问 API Key（或其他支持的 AI 服务）

### 安装与运行

1. **克隆项目**

```bash
git clone https://github.com/putl-dream/ai-boot.git
cd ai-boot
```

2. **配置环境**

创建或编辑环境变量：

```bash
# 必需的环境变量
export DASHSCOPE_API_KEY=your-dashscope-api-key-here
export mysql_pwd=your-mysql-password

# 可选的环境变量（如果使用 OpenAI）
export OPENAI_API_KEY=your-openai-api-key-here
```

或直接在 `src/main/resources/application.yaml` 中配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_boot?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: root
    password: ${mysql_pwd}  # 或直接写入密码
    driver-class-name: com.mysql.cj.jdbc.Driver

  ai:
    # 阿里云通义千问配置
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}

    # OpenAI 配置（可选）
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai.com
```

3. **创建数据库**

执行 `db/table.SQL` 中的 SQL 语句创建数据库表。

4. **构建项目**

```bash
mvn clean install
```

5. **启动应用**

```bash
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动，WebSocket 端点为 `ws://localhost:8080/ws`

### 快速测试

1. **用户注册**

```bash
curl -X POST http://localhost:8080/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com"
  }'
```

2. **用户登录**

```bash
curl -X POST http://localhost:8080/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

3. **使用 WebSocket 客户端连接并发送消息**

连接地址：`ws://localhost:8080/ws?token={JWT_TOKEN}`

发送消息示例：
```json
{
  "type": "chat",
  "from": "user123",
  "to": "ai",
  "content": "你好，请介绍一下你自己",
  "time": "2025-10-24 10:00:00",
  "msgType": "text"
}
```

返回消息示例（流式响应）：
```json
{
  "type": "chat",
  "formType": "assistant",
  "content": "你好！我是",
  "time": "2025-10-24 10:00:01",
  "msgType": "text"
}
```

## API 文档

### 用户认证 API

#### 用户注册
```http
POST /user/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com"
}
```

#### 用户登录
```http
POST /user/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}
```

响应：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": "123",
    "username": "testuser"
  }
}
```

### WebSocket 消息格式

所有 WebSocket 消息都需要包含 `type` 字段用于路由。

#### 聊天消息（type: "chat"）
```json
{
  "type": "chat",
  "from": "userId",
  "to": "ai",
  "content": "用户消息内容",
  "time": "2025-10-24 10:00:00",
  "msgType": "text"
}
```

## 架构概览

```
┌─────────────────┐
│   认证拦截层    │  ← JWT身份验证 (JwtAuthenticationInterceptor)
└────────┬────────┘
         │
┌────────▼────────┐
│  权限校验层     │  ← RBAC权限检查 (PermissionAspect)
└────────┬────────┘
         │
┌────────▼────────┐
│  WebSocket 层   │  ← 客户端连接 (WebSocketHandler)
└────────┬────────┘
         │
┌────────▼────────┐
│  消息路由层     │  ← 自动分发消息 (MessageRouter)
└────────┬────────┘
         │
┌────────▼────────┐
│  业务处理层     │  ← MessageHandler 实现 (ChatService等)
└────────┬────────┘
         │
┌────────▼────────┐
│   AI 模型层     │  ← 多模型支持 (ChatModelFactory)
└─────────────────┘
```

### 设计思想

> **业务与技术分离设计**：模块之间通过接口进行调用，模块之间相互独立，互不依赖。

## 核心模块

### 1. WebSocket Communication Module (WebSocket 通信模块)

WebSocket 通信模块提供实时双向通信能力。

**核心组件**：
- `WebSocketConfig` - WebSocket 配置，默认端点 `/ws`
- `WebSocketHandler` - WebSocket 处理器，处理连接建立、消息接收、连接关闭等事件
- `MessageRouter` - 消息路由器，自动分发到对应处理器
- `SessionManager` - 会话管理
- `MessagePublisher` - 消息发布

**消息处理流程**：
1. 客户端通过 WebSocket 发送消息
2. `WebSocketHandler` 接收原始消息
3. `MessageRouter` 根据消息类型路由到对应的 `MessageHandler`
4. 处理器处理消息并通过 `MessagePublisher` 返回响应

#### 扩展自定义消息类型

实现 `MessageHandler` 接口即可自动注册到路由系统：

```java
@Service
public class CustomService implements MessageHandler {
    @Override
    public String getType() {
        return "custom";  // 消息类型
    }

    @Override
    public void handleMessage(String userId, BaseMessage message) {
        // 处理自定义消息
    }
}
```

消息实体需继承 [BaseMessage](src/main/java/fun/aiboot/websocket/domain/BaseMessage.java)，参考 [ChatMessage](src/main/java/fun/aiboot/websocket/domain/ChatMessage.java)。

完整实现参考 [ChatService.java](src/main/java/fun/aiboot/servicews/ChatService.java)

### 2. Dialogue LLM Module (对话 AI 模块)

提供统一的 AI 模型访问接口，支持多种 AI 服务。

**核心组件**：
- `ChatModelFactory` - 工厂模式创建 AI 模型实例
- `ModelFrameworkType` - 支持的模型类型枚举
- `GlobalToolRegistry` - 全局工具注册表
- `GlobalTool` - 工具调用接口

**支持的模型**：
- 阿里云通义千问 (Dashscope)
- OpenAI

#### 使用示例

```java
@Autowired
private ChatModelFactory chatModelFactory;

// 获取模型实例
ChatModel model = chatModelFactory.takeChatModel(ModelFrameworkType.dashscope);

// 流式调用
Flux<String> stream = model.stream("你好");
stream.subscribe(chunk -> System.out.print(chunk));
```

#### 工具调用 (Function Calling)

实现 `GlobalTool` 接口创建自定义工具：

```java
@Component
public class WeatherTool implements GlobalTool {
    @Override
    public ToolCallback getFunctionCallTool() {
        return ToolCallback.from(
            "get_weather",
            "获取天气信息",
            this::getWeather
        );
    }

    @Override
    public String getPermission() {
        return "weather-tool";
    }

    public String getWeather(String city) {
        return "晴天，25°C";
    }
}
```

工具会自动注册到 `GlobalToolRegistry`，AI 可在对话中智能调用。

### 3. Security Module (安全模块)

提供 JWT 身份认证和 RBAC 权限管理功能。

**核心组件**：
- `JwtUtil` - JWT 工具类，负责生成和解析 JWT
- `JwtAuthenticationInterceptor` - JWT 认证拦截器
- `UserContext` - 用户上下文，存储当前登录用户信息
- `PermissionService` - 权限服务，负责权限校验
- `@RequireRole` - 角色权限注解
- `@RequireTool` - 工具权限注解

#### 使用示例

```java
@RestController
@RequestMapping("/user")
public class UserController {
    
    // 需要管理员角色才能访问
    @GetMapping("/admin")
    @RequireRole("admin")
    public Result<String> adminOnly() {
        return Result.success("欢迎管理员：" + UserContextHolder.getUsername());
    }
    
    // 需要特定工具权限才能访问
    @GetMapping("/impl-test")
    @RequireTool("image-generator")
    public Result<String> toolTest() {
        return Result.success("您拥有图片生成工具权限");
    }
}
```

### 4. Business Layer (业务层)

实现具体业务逻辑，如用户管理、聊天服务等。

**核心服务**：
- `UserService` - 用户服务，处理用户注册、登录、密码管理等
- `AuthService` - 认证服务，处理用户身份验证和Token生成
- `ChatService` - 聊天服务，处理对话消息并集成 AI 模型
- `PermissionService` - 权限服务，处理角色和工具权限管理
- `ModelServices` - 模型服务，管理AI模型配置

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 编程语言 |
| Spring Boot | 3.5.6 | 应用框架 |
| Spring AI | 1.0.0 | AI 集成框架 |
| Spring Security | - | 密码加密 |
| WebSocket | - | 实时通信 |
| Alibaba Dashscope | 1.0.0.3 | 通义千问 SDK |
| Fastjson2 | 2.0.59 | JSON 处理 |
| MyBatis Plus | 3.5.14 | ORM 框架 |
| MySQL | 8.0.15 | 数据库 |
| Lombok | - | 代码简化 |
| JJWT | 1.1.0 | JWT 处理 |

## 项目结构

```
ai-boot/
├── db/                                     # 数据库脚本
│   └── table.SQL                          # 数据库表结构
├── src/main/java/fun/aiboot/
│   ├── AiBootApplication.java             # 应用入口
│   ├── common/                            # 通用模块
│   │   ├── annotation/                    # 权限注解（@RequireRole, @RequireTool）
│   │   ├── aspect/                        # 权限校验切面
│   │   ├── context/                       # 用户上下文
│   │   ├── exception/                     # 异常定义与全局异常处理
│   │   ├── initialize/                    # 初始化组件
│   │   └── interceptor/                   # 拦截器（JWT认证等）
│   ├── config/                            # 配置类（密码加密、WebMVC等）
│   ├── controller/                        # REST控制器
│   ├── dialogue/llm/                      # AI对话模块
│   │   ├── config/                        # 对话配置（记忆、模型配置）
│   │   ├── context/                       # 对话上下文管理
│   │   ├── impl/                          # LLM服务实现
│   │   ├── model/                         # 模型工厂
│   │   ├── persona/                       # 人设提供者
│   │   ├── providers/                     # 模型提供者（Dashscope, OpenAI）
│   │   └── tool/                          # 工具调用（GlobalTool接口）
│   ├── entity/                            # 实体类
│   ├── mapper/                            # MyBatis Mapper接口
│   ├── service/                           # 基础业务服务接口
│   │   └── impl/                          # 基础业务服务实现
│   ├── services/                          # 高级业务服务（Auth, Permission）
│   │   └── impl/                          # 高级业务服务实现
│   ├── servicews/                         # WebSocket业务服务（ChatService）
│   ├── utils/                             # 工具类（JWT, AES加密等）
│   └── websocket/                         # WebSocket通信模块
│       ├── config/                        # WebSocket配置
│       ├── domain/                        # 消息实体
│       └── server/                        # WebSocket服务端实现
├── src/main/resources/
│   ├── application.yaml                   # 应用配置
│   └── application.properties             # 应用配置（备用）
├── src/test/java/fun/aiboot/              # 测试代码
├── TECHNICAL_DOCUMENTATION.md             # 技术文档
├── SECURITY_IMPLEMENTATION.md             # 安全实现说明
├── Permission.md                          # 权限系统说明
├── README.md                              # 项目说明
└── pom.xml                                # Maven 配置
```

## 文档

- **[技术文档](TECHNICAL_DOCUMENTATION.md)** - 详细的架构设计、API 说明、开发指南
- **[安全实现说明](SECURITY_IMPLEMENTATION.md)** - 安全机制和加密说明
- **[权限系统说明](Permission.md)** - RBAC权限系统详解
- **[快速开始](#快速开始)** - 安装与配置指南
- **[核心模块](#核心模块)** - 模块功能说明

## 使用场景

- **智能客服系统** - 实时对话，自动回复
- **AI 助手应用** - 支持工具调用的智能助手
- **知识问答系统** - 基于 AI 的问答服务
- **物联网设备对话** - ESP32 等设备的语音交互
- **企业级聊天机器人** - 可扩展的企业对话解决方案
- **教育辅导系统** - 智能答疑和学习辅助
- **技术支持系统** - 自动化技术问题解答

## 最近更新

### v0.0.1-SNAPSHOT (2025-11)
- 重构工具调用管理逻辑，优化 GlobalTool 架构
- 重构工具调用模块包结构和类名
- 增强对话日志记录与 WebSocket 连接关闭信息
- 优化日志记录级别和内容
- 完善权限系统和安全机制

## 路线图

- [x] WebSocket 通信模块
- [x] 阿里云通义千问集成
- [x] 流式响应支持
- [x] Function Calling 工具调用
- [x] JWT 身份认证
- [x] RBAC 权限管理
- [x] 全局异常处理
- [x] OpenAI 模型集成
- [x] 对话历史管理
- [x] 消息持久化
- [x] 工具调用权限管理
- [ ] 多轮对话上下文优化
- [ ] 上下文总结精简提高对话连贯性
- [ ] RAG数据库支持（向量数据库集成）
- [ ] Function 函数映射入库
- [ ] 集群部署支持
- [ ] 更丰富的权限控制策略
- [ ] 审计日志功能
- [ ] 后台管理系统
- [ ] WebUI 管理界面

## 常见问题

### 如何添加自定义工具？

实现 `GlobalTool` 接口并添加 `@Component` 注解即可自动注册：

```java
@Component
public class MyCustomTool implements GlobalTool {
    @Override
    public ToolCallback getFunctionCallTool() {
        return ToolCallback.from(
            "my_tool",
            "工具描述",
            this::myToolMethod
        );
    }

    @Override
    public String getPermission() {
        return "my-custom-tool";
    }

    public String myToolMethod(String param) {
        // 工具实现
        return "result";
    }
}
```

### 如何切换 AI 模型？

在调用 LLM 服务时指定模型类型：

```java
// 使用通义千问
llmService.stream(userId, "dashscope", content);

// 使用 OpenAI
llmService.stream(userId, "openai", content);
```

### 如何自定义消息类型？

1. 创建继承 `BaseMessage` 的消息类
2. 实现 `MessageHandler` 接口处理该消息
3. 系统会自动注册并路由

```java
@Service
public class MyMessageHandler implements MessageHandler {
    @Override
    public String getType() {
        return "my-type";
    }

    @Override
    public void handleMessage(String userId, BaseMessage message) {
        // 处理逻辑
    }
}
```

### WebSocket 连接失败怎么办？

1. 确认 JWT Token 是否有效
2. 检查 Token 是否通过查询参数 `?token=xxx` 传递
3. 查看服务端日志确认拦截器是否通过
4. 确认 WebSocket 端点 `/ws` 是否正确

### 如何管理用户权限？

系统使用 RBAC 模型，可以通过以下方式控制权限：

1. **角色权限**：使用 `@RequireRole("admin")` 注解
2. **工具权限**：使用 `@RequireTool("tool-name")` 注解
3. 在数据库中配置用户-角色和角色-工具的映射关系

## 贡献

欢迎贡献代码、报告问题或提出改进建议！

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 联系方式

如有问题或建议，欢迎通过以下方式联系：

- 提交 Issue
- 发起 Pull Request
- 邮件联系项目维护者

---

**Built with ❤️ using Spring Boot and Spring AI**
