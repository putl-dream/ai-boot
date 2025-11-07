# AI-Boot 技术文档

## 目录
- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [架构设计](#架构设计)
- [核心模块详解](#核心模块详解)
- [快速开始](#快速开始)
- [使用示例](#使用示例)
- [扩展开发指南](#扩展开发指南)
- [最佳实践](#最佳实践)

---

## 项目概述

AI-Boot 是一个基于 Spring Boot 和 Spring AI 构建的智能对话系统框架，旨在提供一个高度可扩展、模块化的 AI 应用开发基础设施。

### 核心特性

- **模块化架构**: 业务与技术分离，模块间通过接口松耦合
- **多模型支持**: 支持阿里云通义千问、OpenAI 等多种 AI 模型
- **WebSocket 通信**: 实时双向通信，支持流式响应
- **工具调用**: 支持 Function Calling，扩展 AI 能力
- **消息路由**: 基于类型的自动消息分发机制
- **流式响应**: 支持 AI 响应的实时流式传输

---

## 技术栈

### 核心框架
- **Java**: 21
- **Spring Boot**: 3.5.6
- **Spring AI**: 1.0.0
- **Maven**: 构建工具

### 主要依赖
- **spring-boot-starter-web**: Web 应用支持
- **spring-boot-starter-websocket**: WebSocket 支持
- **spring-ai-openai**: OpenAI 集成
- **spring-ai-alibaba-starter-dashscope**: 阿里云通义千问集成
- **fastjson2**: JSON 序列化/反序列化
- **lombok**: 简化 Java 代码
- **spring-boot-starter-actuator**: 应用监控
- **spring-boot-starter-aop**: AOP 支持
- **spring-security-crypto**: 密码加密
- **mysql-connector-java**: MySQL 数据库连接
- **mybatis-plus-boot-starter**: MyBatis Plus ORM 框架

---

## 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│                    (WebSocket Client)                        │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   Communication Module                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ WebSocket    │  │ Message      │  │ Session      │      │
│  │ Handler      │─→│ Router       │  │ Manager      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   Business Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Message      │  │ Message      │  │ Message      │      │
│  │ Handler 1    │  │ Handler 2    │  │ Handler N    │      │
│  │ (ChatService)│  │              │  │              │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   Dialogue LLM Module                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Chat Model   │  │ Tool         │  │ Model        │      │
│  │ Factory      │  │ Registry     │  │ Providers    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   External AI Services                       │
│         (Alibaba Dashscope, OpenAI, etc.)                   │
└─────────────────────────────────────────────────────────────┘
```

### 设计原则

1. **单一职责**: 每个模块专注于特定功能
2. **开闭原则**: 对扩展开放，对修改封闭
3. **依赖倒置**: 依赖抽象接口而非具体实现
4. **接口隔离**: 通过接口实现模块间解耦

---

## 核心模块详解

### 1. Communication Module (通信模块)

通信模块负责处理客户端与服务端之间的 WebSocket 通信，提供消息路由和会话管理功能。

#### 1.1 核心组件

##### WebSocketConfig
配置类，注册 WebSocket 端点。

**位置**: `fun.aiboot.websocket.config.WebSocketConfig`

**配置项**:
- `socket.path`: WebSocket 端点路径，默认 `/ws`

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Value("${socket.path:/ws}")
    private String WS_PATH;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, WS_PATH)
                .setAllowedOrigins("*");
    }
}
```

##### WebSocketHandler
处理 WebSocket 连接的建立、消息接收、连接关闭等事件。

**位置**: `fun.aiboot.websocket.server.WebSocketHandler`

**主要方法**:
- `afterConnectionEstablished`: 连接建立时触发
- `handleTextMessage`: 接收到文本消息时触发
- `afterConnectionClosed`: 连接关闭时触发

##### MessageRouter
消息路由器，根据消息类型自动分发到对应的处理器。

**位置**: `fun.aiboot.websocket.server.DefaultMessageRouter`

**工作流程**:
1. 接收原始 JSON 消息
2. 解析消息类型
3. 查找对应的 MessageHandler
4. 调用处理器处理消息

```java
@Service
public class DefaultMessageRouter implements MessageRouter {
    private final Map<String, MessageHandler> handlers = new HashMap<>();

    @Autowired
    public DefaultMessageRouter(List<MessageHandler> handlerList) {
        handlerList.forEach(handler ->
            handlers.put(handler.getType(), handler));
    }

    @Override
    public void route(String userId, String rawMessage) {
        var base = JSON.to(BaseMessage.class, rawMessage);
        String type = base.getType();
        MessageHandler handler = handlers.get(type);
        handler.handleMessage(userId, base);
    }
}
```

##### SessionManager
会话管理器，维护用户 WebSocket 会话。

**位置**: `fun.aiboot.websocket.server.WebSocketSessionManagerImpl`

**功能**:
- 添加/移除会话
- 根据用户ID查找会话
- 检查会话是否在线

##### MessagePublisher
消息发布器，向客户端发送消息。

**位置**: `fun.aiboot.websocket.server.WebSocketPublisherImpl`

**功能**:
- 向指定用户发送消息
- 广播消息到所有在线用户

#### 1.2 消息模型

##### BaseMessage
所有消息的基类，使用 Jackson 多态支持。

**位置**: `fun.aiboot.websocket.domain.BaseMessage`

```java
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ChatMessage.class, name = "chat"),
})
public sealed abstract class BaseMessage permits ChatMessage {
    protected String type;
}
```

##### ChatMessage
聊天消息实体。

**位置**: `fun.aiboot.websocket.domain.ChatMessage`

**字段**:
- `from`: 发送者ID
- `to`: 接收者ID
- `content`: 消息内容
- `time`: 发送时间
- `msgType`: 消息类型（如 text、image）

#### 1.3 扩展自定义消息类型

要添加新的消息类型，需要：

1. 继承 `BaseMessage` 并在 `@JsonSubTypes` 中注册
2. 实现 `MessageHandler` 接口
3. 使用 `@Service` 注解自动注册到路由器

**示例**:
```java
// 1. 定义消息实体
@Data
@EqualsAndHashCode(callSuper = true)
public final class OrderMessage extends BaseMessage {
    private String orderId;
    private String status;

    public OrderMessage() {
        super("order");
    }
}

// 2. 更新 BaseMessage
@JsonSubTypes({
    @JsonSubTypes.Type(value = ChatMessage.class, name = "chat"),
    @JsonSubTypes.Type(value = OrderMessage.class, name = "order"),
})
public sealed abstract class BaseMessage
    permits ChatMessage, OrderMessage { }

// 3. 实现处理器
@Service
public class OrderService implements MessageHandler {
    @Override
    public String getType() {
        return "order";
    }

    @Override
    public void handleMessage(String userId, BaseMessage message) {
        OrderMessage msg = (OrderMessage) message;
        // 处理订单消息
    }
}
```

---

### 2. Dialogue LLM Module (对话 AI 模块)

对话模块负责与各种 AI 模型交互，提供统一的对话接口和工具调用能力。

#### 2.1 核心组件

##### ChatModelFactory
AI 模型工厂，使用工厂模式创建不同的 AI 模型实例。

**位置**: `fun.aiboot.dialogue.llm.model.ChatModelFactory`

**支持的模型**:
- `dashscope`: 阿里云通义千问
- `openai`: OpenAI 模型

```java
@Component
public class ChatModelFactory {
    public ChatModel takeChatModel(ModelFrameworkType modelFrameworkType) {
        return switch (modelFrameworkType) {
            case dashscope -> DashscopeModel.builder()
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    .modelName("qwen3-max")
                    .toolCallingManager(toolCallingManager)
                    .toolsGlobalRegistry(toolsGlobalRegistry)
                    .build();
            case openai -> OpenAIModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName("gpt-4")
                    .toolCallingManager(toolCallingManager)
                    .toolsGlobalRegistry(toolsGlobalRegistry)
                    .build();
        };
    }
}
```

##### DashscopeModel
阿里云通义千问模型的实现。

**位置**: `fun.aiboot.dialogue.llm.providers.DashscopeLlmProvider`

**功能**:
- 同步对话调用
- 流式对话支持
- 工具调用集成

##### OpenAIModel
OpenAI 模型的实现。

**位置**: `fun.aiboot.dialogue.llm.providers.OpenAiLlmProvider`

**功能**:
- 同步对话调用
- 流式对话支持
- 工具调用集成

##### ToolsGlobalRegistry
全局工具注册表，管理所有可用的 Function Calling 工具。

**位置**: `fun.aiboot.dialogue.llm.function.ToolsGlobalRegistry`

**功能**:
- 注册/注销工具
- 解析工具调用
- 维护工具列表

```java
@Service
public class ToolsGlobalRegistry implements ToolCallbackResolver {
    protected static final ConcurrentHashMap<String, ToolCallback> allFunction
            = new ConcurrentHashMap<>();

    @Override
    public ToolCallback resolve(@NotNull String toolName) {
        return allFunction.get(toolName);
    }

    public ToolCallback registerFunction(String name, ToolCallback functionCallTool) {
        return allFunction.putIfAbsent(name, functionCallTool);
    }
}
```

#### 2.2 Function Calling (工具调用)

##### GlobalFunction
全局工具接口，所有工具需实现此接口。

**位置**: `fun.aiboot.dialogue.llm.function.GlobalFunction`

```java
public interface GlobalFunction {
    ToolCallback getFunctionCallTool();
}
```

##### 实现自定义工具

**示例**: 创建一个天气查询工具

```java
@Component
public class WeatherFunction implements GlobalFunction {

    @Override
    public ToolCallback getFunctionCallTool() {
        return ToolCallback.from(
            "get_weather",
            "Get current weather for a location",
            this::getWeather
        );
    }

    public String getWeather(WeatherRequest request) {
        // 实现天气查询逻辑
        return "Temperature: 25°C, Sunny";
    }

    @JsonClassDescription("Weather query request")
    record WeatherRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("City name")
        String city
    ) {}
}
```

工具会自动注册到 `ToolsGlobalRegistry`，AI 模型可以在对话中调用。

---

### 3. Security Module (安全模块)

安全模块提供完整的身份认证和权限管理功能，基于 JWT 和 RBAC 设计。

#### 3.1 核心组件

##### JwtUtil
JWT 工具类，负责生成和解析 JWT token。

**位置**: `fun.aiboot.utils.JwtUtil`

**功能**:
- 生成 JWT token
- 验证和解析 JWT token
- 提取用户信息

##### JwtAuthenticationInterceptor
JWT 认证拦截器，拦截所有请求并验证 JWT token。

**位置**: `fun.aiboot.common.interceptor.JwtAuthenticationInterceptor`

**特性**:
- 从请求头`Authorization`中提取token
- 支持`Bearer`前缀
- 解析token并将用户信息存入ThreadLocal
- 请求完成后自动清理上下文

##### UserContext
用户上下文，使用ThreadLocal存储当前登录用户信息。

**位置**: `fun.aiboot.common.context.UserContext`

```java
String userId = UserContext.getUserId();
String username = UserContext.getUsername();
```

##### PermissionService
权限服务，负责权限校验核心逻辑。

**位置**: `fun.aiboot.services.PermissionService`

**功能**:
- 检查用户是否拥有指定角色
- 检查用户是否拥有指定工具权限
- 获取用户的所有角色
- 获取用户的所有工具（包括角色继承的工具）

##### @RequireRole
角色权限校验注解。

**位置**: `fun.aiboot.common.annotation.RequireRole`

```java
// 需要拥有admin角色
@RequireRole("admin")

// 需要拥有admin或vip角色之一
@RequireRole({"admin", "vip"})

// 需要同时拥有admin和vip角色
@RequireRole(value = {"admin", "vip"}, requireAll = true)
```

##### @RequireTool
工具权限校验注解。

**位置**: `fun.aiboot.common.annotation.RequireTool`

```java
// 需要拥有image-generator工具权限
@RequireTool("image-generator")

// 需要拥有指定的多个工具权限
@RequireTool(value = {"advanced-search", "data-export"}, requireAll = true)
```

#### 3.2 权限设计

项目采用RBAC（基于角色的访问控制）+ 工具权限的混合模型：

1. **用户 ← 角色**：一个用户可以拥有多个角色
2. **角色 ← 工具**：一个角色可以关联多个工具
3. **用户 ← 工具**：用户也可以直接拥有工具权限

权限继承关系：
- 用户的最终工具权限 = 用户直接拥有的工具 + 用户所有角色拥有的工具

#### 3.3 密码加密

使用BCrypt算法加密密码，确保密码安全。

**位置**: `fun.aiboot.config.PasswordEncoderConfig`

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

---

## 4. Business Layer (业务层)

业务层实现具体的业务逻辑，包括用户管理、会话管理、消息处理等核心功能。

### 4.1 核心服务

#### UserService
用户服务，处理用户注册、登录、密码管理等。

**位置**: `fun.aiboot.service.UserService`

**功能**:
- 用户注册
- 用户登录
- 密码修改
- 用户信息管理

#### ChatService
聊天服务，处理聊天消息并调用 AI 模型生成响应。

**位置**: `fun.aiboot.servicews.ChatService`

**功能**:
- 接收用户消息
- 调用 AI 模型
- 流式响应处理
- 将响应发送给用户

```java
@Service
public class ChatService implements MessageHandler {

    @Autowired
    private ChatModelFactory chatModelFactory;

    @Autowired
    private MessagePublisher messagePublisher;

    @Override
    public String getType() {
        return "chat";
    }

    @Override
    public void handleMessage(String userId, BaseMessage message) {
        ChatMessage msg = (ChatMessage) message;
        Flux<String> stream = chatModelFactory
            .takeChatModel(ModelFrameworkType.dashscope)
            .stream(msg.getContent());

        StringBuilder responseBuilder = new StringBuilder();

        stream.subscribe(
            chunk -> {
                responseBuilder.append(chunk);
                ChatMessage chatMessage = new ChatMessage(
                    msg.getFrom(),
                    msg.getTo(),
                    chunk,
                    LocalDateTime.now(),
                    "text"
                );
                messagePublisher.sendToUser(userId, chatMessage);
            },
            error -> {
                log.error("Error in streaming: " + error.getMessage());
            },
            () -> {
                log.info("Stream completed: {}", responseBuilder);
            }
        );
    }
}
```

### 4.2 数据库服务

项目实现了完整的数据库服务层，基于 MyBatis Plus 实现，提供完整的 CRUD 操作：

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

所有服务均遵循 Service/Impl 模式，便于扩展和维护。

### 4.3 数据加密

项目实现了数据加解密功能，保护敏感信息：

#### AesGcmUtil
AES-GCM 加解密工具类，提供对称加密功能。

**位置**: `fun.aiboot.utils.AesGcmUtil`

**功能**:
- 数据加密
- 数据解密
- 安全的密钥管理

```java
// 加密
String encrypted = AesGcmUtil.encrypt("sensitive data");

// 解密
String decrypted = AesGcmUtil.decrypt(encrypted);
```

### 4.4 系统提示词管理

系统提示词管理功能允许管理员配置不同的 AI 角色和行为：

#### SysPromptService
系统提示词服务，管理 AI 的角色设定和行为规范。

**位置**: `fun.aiboot.service.ModelRoleService`

**功能**:
- 提示词管理
- 角色行为配置
- 动态提示词更新

---

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+
- MySQL 8.0+
- 通义千问 API Key（或其他支持的 AI 服务）
- OpenAI API Key（可选）

### 配置步骤

#### 1. 克隆项目

```bash
git clone <repository-url>
cd ai-boot
```

#### 2. 配置环境变量

设置必要的环境变量：
```bash
export DASHSCOPE_API_KEY=your-dashscope-api-key-here
export OPENAI_API_KEY=your-openai-api-key-here
export mysql_pwd=your-mysql-password
```

#### 3. 配置数据库

在 `src/main/resources/application.yaml` 中配置数据库密码：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_boot?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: root
    password: your-mysql-password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

#### 4. 创建数据库表

执行 `db/table.SQL` 中的 SQL 语句创建数据库表。

#### 5. 构建项目

```bash
mvn clean install
```

#### 6. 运行项目

```bash
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动，WebSocket 端点为 `ws://localhost:8080/ws`。

### 测试 WebSocket 连接

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

发送消息：
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

服务端将流式返回 AI 响应。

---

## 使用示例

### 示例 1: 基本聊天

**客户端代码** (JavaScript):

```javascript
const ws = new WebSocket('ws://localhost:8080/ws');

ws.onopen = () => {
  console.log('WebSocket connected');

  // 发送聊天消息
  const message = {
    type: 'chat',
    from: 'user123',
    to: 'ai',
    content: '写一首关于春天的诗',
    time: new Date().toISOString(),
    msgType: 'text'
  };

  ws.send(JSON.stringify(message));
};

ws.onmessage = (event) => {
  const response = JSON.parse(event.data);
  console.log('AI响应:', response.content);
  // 显示响应内容
};

ws.onerror = (error) => {
  console.error('WebSocket error:', error);
};

ws.onclose = () => {
  console.log('WebSocket disconnected');
};
```

### 示例 2: 使用工具调用

**定义工具**:

```java
@Component
public class CalculatorFunction implements GlobalFunction {

    @Override
    public ToolCallback getFunctionCallTool() {
        return ToolCallback.from(
            "calculate",
            "Perform mathematical calculations",
            this::calculate
        );
    }

    public String calculate(CalculateRequest request) {
        double result = switch (request.operation()) {
            case "add" -> request.a() + request.b();
            case "subtract" -> request.a() - request.b();
            case "multiply" -> request.a() * request.b();
            case "divide" -> request.a() / request.b();
            default -> 0;
        };
        return String.valueOf(result);
    }

    record CalculateRequest(
        @JsonProperty(required = true) double a,
        @JsonProperty(required = true) double b,
        @JsonProperty(required = true) String operation
    ) {}
}
```

**使用**:

发送消息："帮我计算 123 加 456 等于多少？"

AI 会自动调用 `calculate` 工具并返回结果。

### 示例 3: 扩展自定义消息处理器

**场景**: 添加图片消息处理

```java
// 1. 定义消息实体
@Data
@EqualsAndHashCode(callSuper = true)
public final class ImageMessage extends BaseMessage {
    private String imageUrl;
    private String caption;

    public ImageMessage() {
        super("image");
    }
}

// 2. 实现处理器
@Service
public class ImageService implements MessageHandler {

    @Override
    public String getType() {
        return "image";
    }

    @Override
    public void handleMessage(String userId, BaseMessage message) {
        ImageMessage msg = (ImageMessage) message;
        // 处理图片消息，例如：图片识别、OCR等
        log.info("Received image: {}", msg.getImageUrl());
    }
}
```

---

## 扩展开发指南

### 添加新的 AI 模型

#### 1. 实现模型提供者

创建新的模型实现类，继承相应的基类：

```java
@Component
public class GeminiModel extends DashscopeModel {
    // 实现特定于 Gemini 的功能
}
```

#### 2. 在工厂中注册

更新 `ChatModelFactory`:

```java
public ChatModel takeChatModel(ModelFrameworkType modelFrameworkType) {
    return switch (modelFrameworkType) {
        case dashscope -> DashscopeModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("qwen3-max")
                .toolCallingManager(toolCallingManager)
                .toolsGlobalRegistry(toolsGlobalRegistry)
                .build();
        case openai -> OpenAIModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4")
                .toolCallingManager(toolCallingManager)
                .toolsGlobalRegistry(toolsGlobalRegistry)
                .build();
        case gemini -> GeminiModel.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .modelName("gemini-pro")
                .toolCallingManager(toolCallingManager)
                .toolsGlobalRegistry(toolsGlobalRegistry)
                .build();
    };
}
```

### 自定义消息路由策略

如果需要更复杂的路由逻辑，可以实现自定义 `MessageRouter`:

```java
@Service
@Primary
public class CustomMessageRouter implements MessageRouter {

    @Override
    public void route(String userId, String rawMessage) {
        // 自定义路由逻辑
        // 例如：基于用户权限、消息优先级等
    }
}
```

### 添加消息拦截器

可以添加拦截器在消息处理前后执行额外逻辑：

```java
@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) {
        // 消息处理前的逻辑
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                               HttpServletResponse response,
                               Object handler,
                               Exception ex) {
        // 消息处理后的逻辑
    }
}
```

### 实现自定义工具调用

创建新的工具功能：

```java
@Component
public class DatabaseQueryFunction implements GlobalFunction {

    @Override
    public ToolCallback getFunctionCallTool() {
        return ToolCallback.from(
            "database_query",
            "Query database for information",
            this::queryDatabase
        );
    }

    public String queryDatabase(QueryRequest request) {
        // 实现数据库查询逻辑
        return "Query result";
    }

    record QueryRequest(
        @JsonProperty(required = true) String query
    ) {}
}
```

### 扩展权限管理系统

可以添加新的权限实体和服务：

1. 创建新的权限实体类
2. 实现相应的 Mapper 接口
3. 创建 Service 和 ServiceImpl 类
4. 在权限校验切面中添加新的校验逻辑

---

## 最佳实践

### 1. 异常处理

在 MessageHandler 中添加完善的异常处理：

```java
@Override
public void handleMessage(String userId, BaseMessage message) {
    try {
        // 业务逻辑
    } catch (Exception e) {
        log.error("Error handling message for user {}: {}",
                 userId, e.getMessage(), e);
        // 向用户发送错误消息
        ErrorMessage errorMsg = new ErrorMessage(e.getMessage());
        messagePublisher.sendToUser(userId, errorMsg);
    }
}
```

### 2. 日志记录

使用结构化日志记录关键操作：

```java
log.info("User {} sent message type: {}, content length: {}",
         userId, message.getType(), message.getContent().length());
```

### 3. 性能优化

- 使用流式响应减少用户等待时间
- 对长时间运行的任务使用异步处理
- 合理配置 WebSocket 连接池和线程池
- 使用数据库连接池优化数据库访问
- 合理使用缓存减少重复计算

### 4. 安全考虑

- 验证 WebSocket 连接身份
- 对消息内容进行校验和过滤
- 限制消息大小和频率
- 保护 API Key 等敏感信息
- 使用 JWT 进行身份验证
- 实施 RBAC 权限控制
- 敏感数据加密存储

```java
@Component
public class WebSocketAuthInterceptor extends HttpSessionHandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                  ServerHttpResponse response,
                                  WebSocketHandler wsHandler,
                                  Map<String, Object> attributes) {
        // 验证用户身份
        return true;
    }
}
```

### 5. 监控和诊断

使用 Spring Boot Actuator 监控应用状态：

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

访问监控端点：
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/metrics`

### 6. 数据库设计最佳实践

- 合理设计表结构和索引
- 使用事务确保数据一致性
- 定期备份重要数据
- 实施数据归档策略

### 7. AI 模型使用最佳实践

- 合理设置模型参数
- 实施重试机制处理模型调用失败
- 监控模型调用成本
- 定期评估模型性能

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

访问监控端点：
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/metrics`

---

## 常见问题

### Q1: WebSocket 连接断开如何处理？

实现心跳机制和自动重连：

```javascript
let ws;
let heartbeatTimer;

function connect() {
  ws = new WebSocket('ws://localhost:8080/ws?token=YOUR_JWT_TOKEN');

  ws.onopen = () => {
    startHeartbeat();
  };

  ws.onclose = () => {
    stopHeartbeat();
    setTimeout(connect, 3000); // 3秒后重连
  };
}

function startHeartbeat() {
  heartbeatTimer = setInterval(() => {
    if (ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({type: 'ping'}));
    }
  }, 30000); // 每30秒发送心跳
}
```

### Q2: 如何处理大量并发连接？

- 使用负载均衡
- 增加服务器实例
- 使用消息队列进行削峰
- 优化数据库查询
- 合理配置线程池和连接池

### Q3: AI 响应太慢怎么办？

- 使用流式响应提升用户体验
- 缓存常见问题答案
- 优化提示词减少 token 消耗
- 选择更快的模型
- 实施模型调用异步处理

### Q4: 如何管理不同用户的权限？

项目提供了完整的 RBAC 权限管理系统：

- 通过 `@RequireRole` 注解控制角色访问权限
- 通过 `@RequireTool` 注解控制工具访问权限
- 使用 `PermissionService` 进行编程式权限检查
- 通过数据库管理用户、角色和工具的关联关系

### Q5: 如何添加新的 AI 模型支持？

- 实现 `ChatModel` 接口创建新的模型提供者
- 在 `ChatModelFactory` 中注册新的模型
- 配置相应的 API Key 环境变量
- 更新数据库中的模型配置

### Q6: 如何保证数据安全？

- 使用 JWT 进行身份验证
- 敏感数据使用 AES-GCM 加密存储
- 密码使用 BCrypt 加密存储
- 实施完整的 RBAC 权限控制
- 定期更新安全依赖包

---

## 项目结构

```
ai-boot/
├── db/                                     # 数据库脚本
│   └── table.SQL                          # 数据库表结构
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── fun/
│   │   │       └── aiboot/
│   │   │           ├── AiBootApplication.java             # 应用入口
│   │   │           ├── annotation/                        # 权限注解
│   │   │           ├── aspect/                            # 权限校验切面
│   │   │           ├── common/                            # 通用类
│   │   │           ├── config/                            # 配置类
│   │   │           ├── context/                           # 用户上下文
│   │   │           ├── controller/                        # 控制器
│   │   │           ├── entity/                            # 实体类
│   │   │           ├── exception/                         # 异常处理
│   │   │           ├── interceptor/                       # 拦截器
│   │   │           ├── mapper/                            # Mapper接口
│   │   │           ├── service/                           # 业务服务接口
│   │   │           │   └── impl/                          # 业务服务实现
│   │   │           ├── communication/                     # 通信模块
│   │   │           │   ├── config/                        # WebSocket配置
│   │   │           │   ├── domain/                        # 消息实体
│   │   │           │   ├── interceptor/                   # WebSocket拦截器
│   │   │           │   └── server/                        # WebSocket服务端实现
│   │   │           ├── dialogue/                          # AI对话模块
│   │   │           │   └── llm/                           # LLM相关实现
│   │   │           │       ├── config/                    # 对话配置
│   │   │           │       ├── factory/                   # 模型工厂
│   │   │           │       ├── impl/                      # LLM服务实现
│   │   │           │       ├── memory/                    # 对话记忆
│   │   │           │       ├── providers/                 # 模型提供者
│   │   │           │       └── tool/                      # 工具调用
│   │   │           ├── services/                          # 业务服务
│   │   │           ├── utils/                             # 工具类
│   │   │           └── wsservice/                         # WebSocket服务
│   │   └── resources/
│   │       ├── application.yaml                           # 应用配置
│   │       └── application.properties                     # 应用配置（备用）
│   └── test/
│       └── java/
│           └── fun/
│               └── aiboot/                                # 测试代码
├── pom.xml                                                # Maven配置
├── README.md                                              # 项目说明
├── plan.md                                                # 开发计划
├── TECHNICAL_DOCUMENTATION.md                             # 技术文档
└── SECURITY_IMPLEMENTATION.md                             # 安全实现说明
```

---

## 参考资源

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [WebSocket API](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket)
- [Alibaba Cloud Dashscope](https://help.aliyun.com/zh/dashscope/)

---

## 更新日志

### v0.0.1-SNAPSHOT (2025-10-24)
- 初始版本发布
- 实现 WebSocket 通信模块
- 集成阿里云通义千问模型
- 支持流式响应
- 实现 Function Calling 工具调用
- 完成基础架构设计

### v0.0.2-SNAPSHOT (2025-11-05)
- 集成 OpenAI 模型支持
- 实现完整的 RBAC 权限管理系统
- 添加会话和消息持久化功能
- 实现数据加解密功能
- 完善用户认证和授权机制
- 添加系统提示词管理功能

---

## 贡献指南

欢迎贡献代码、报告问题或提出改进建议。

### 开发流程

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- 遵循 Java 命名规范
- 添加必要的注释和文档
- 编写单元测试
- 保持代码简洁清晰

---

