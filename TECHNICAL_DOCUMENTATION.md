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

**位置**: `fun.aiboot.communication.config.WebSocketConfig`

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

**位置**: `fun.aiboot.communication.server.WebSocketHandler`

**主要方法**:
- `afterConnectionEstablished`: 连接建立时触发
- `handleTextMessage`: 接收到文本消息时触发
- `afterConnectionClosed`: 连接关闭时触发

##### MessageRouter
消息路由器，根据消息类型自动分发到对应的处理器。

**位置**: `fun.aiboot.communication.server.DefaultMessageRouter`

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

**位置**: `fun.aiboot.communication.server.WebSocketSessionManagerImpl`

**功能**:
- 添加/移除会话
- 根据用户ID查找会话
- 检查会话是否在线

##### MessagePublisher
消息发布器，向客户端发送消息。

**位置**: `fun.aiboot.communication.server.WebSocketPublisherImpl`

**功能**:
- 向指定用户发送消息
- 广播消息到所有在线用户

#### 1.2 消息模型

##### BaseMessage
所有消息的基类，使用 Jackson 多态支持。

**位置**: `fun.aiboot.communication.domain.BaseMessage`

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

**位置**: `fun.aiboot.communication.domain.ChatMessage`

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

**位置**: `fun.aiboot.dialogue.llm.factory.ChatModelFactory`

**支持的模型**:
- `dashscope`: 阿里云通义千问
- 预留扩展其他模型

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
        };
    }
}
```

##### DashscopeModel
阿里云通义千问模型的实现。

**位置**: `fun.aiboot.dialogue.llm.providers.DashscopeModel`

**功能**:
- 同步对话调用
- 流式对话支持
- 工具调用集成

##### ToolsGlobalRegistry
全局工具注册表，管理所有可用的 Function Calling 工具。

**位置**: `fun.aiboot.dialogue.llm.tool.ToolsGlobalRegistry`

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

**位置**: `fun.aiboot.dialogue.llm.tool.GlobalFunction`

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

### 3. Business Layer (业务层)

业务层实现具体的业务逻辑，通过实现 `MessageHandler` 接口处理不同类型的消息。

#### ChatService
聊天服务，处理聊天消息并调用 AI 模型生成响应。

**位置**: `fun.aiboot.services.ChatService`

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

---

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+
- 通义千问 API Key（或其他支持的 AI 服务）

### 配置步骤

#### 1. 克隆项目

```bash
git clone <repository-url>
cd ai-boot
```

#### 2. 配置 API Key

设置环境变量：
```bash
export DASHSCOPE_API_KEY=your-api-key-here
```

或在 `application.properties` 中配置：
```properties
spring.ai.dashscope.api-key=your-api-key-here
socket.path=/ws
```

#### 3. 构建项目

```bash
mvn clean install
```

#### 4. 运行项目

```bash
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动，WebSocket 端点为 `ws://localhost:8080/ws`。

### 测试 WebSocket 连接

使用 WebSocket 客户端连接到 `ws://localhost:8080/ws`，发送消息：

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

#### 1. 定义模型类型

在 `ModelFrameworkType` 枚举中添加新类型：

```java
public enum ModelFrameworkType {
    dashscope,
    openai,
    gemini  // 新增
}
```

#### 2. 实现模型提供者

创建新的模型实现类：

```java
@Component
public class GeminiModel implements ChatModel {
    // 实现 Spring AI 的 ChatModel 接口
}
```

#### 3. 在工厂中注册

更新 `ChatModelFactory`:

```java
public ChatModel takeChatModel(ModelFrameworkType modelFrameworkType) {
    return switch (modelFrameworkType) {
        case dashscope -> // ...
        case openai -> // ...
        case gemini -> GeminiModel.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
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

### 4. 安全考虑

- 验证 WebSocket 连接身份
- 对消息内容进行校验和过滤
- 限制消息大小和频率
- 保护 API Key 等敏感信息

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

---

## 常见问题

### Q1: WebSocket 连接断开如何处理？

实现心跳机制和自动重连：

```javascript
let ws;
let heartbeatTimer;

function connect() {
  ws = new WebSocket('ws://localhost:8080/ws');

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

### Q3: AI 响应太慢怎么办？

- 使用流式响应提升用户体验
- 缓存常见问题答案
- 优化提示词减少 token 消耗
- 选择更快的模型

---

## 项目结构

```
ai-boot/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── fun/
│   │   │       └── aiboot/
│   │   │           ├── AiBootApplication.java          # 应用入口
│   │   │           ├── communication/                   # 通信模块
│   │   │           │   ├── config/
│   │   │           │   │   └── WebSocketConfig.java    # WebSocket配置
│   │   │           │   ├── domain/
│   │   │           │   │   ├── BaseMessage.java        # 消息基类
│   │   │           │   │   └── ChatMessage.java        # 聊天消息
│   │   │           │   └── server/
│   │   │           │       ├── MessageHandler.java     # 消息处理器接口
│   │   │           │       ├── MessageRouter.java      # 消息路由器接口
│   │   │           │       ├── DefaultMessageRouter.java # 默认路由实现
│   │   │           │       ├── WebSocketHandler.java   # WebSocket处理器
│   │   │           │       ├── SessionManager.java     # 会话管理器接口
│   │   │           │       ├── WebSocketSessionManagerImpl.java
│   │   │           │       ├── MessagePublisher.java   # 消息发布器接口
│   │   │           │       └── WebSocketPublisherImpl.java
│   │   │           ├── dialogue/                        # 对话模块
│   │   │           │   └── llm/
│   │   │           │       ├── factory/
│   │   │           │       │   ├── ChatModelFactory.java # 模型工厂
│   │   │           │       │   └── ModelFrameworkType.java # 模型类型枚举
│   │   │           │       ├── providers/
│   │   │           │       │   └── DashscopeModel.java  # 通义千问实现
│   │   │           │       └── tool/
│   │   │           │           ├── GlobalFunction.java  # 工具接口
│   │   │           │           ├── ToolsGlobalRegistry.java # 工具注册表
│   │   │           │           └── function/
│   │   │           │               ├── TestFunction.java
│   │   │           │               └── Test1Function.java
│   │   │           └── service/
│   │   │               └── ChatService.java             # 聊天服务
│   │   └── resources/
│   │       └── application.properties                   # 应用配置
│   └── test/
│       └── java/
│           └── fun/
│               └── aiboot/
│                   └── XiaoAiEsp32ApplicationTests.java
├── pom.xml                                              # Maven配置
├── README.md                                            # 项目说明
└── TECHNICAL_DOCUMENTATION.md                           # 技术文档
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

