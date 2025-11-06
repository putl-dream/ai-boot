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
git clone <repository-url>
cd ai-boot
```

2. **配置环境**

设置环境变量：
```bash
export DASHSCOPE_API_KEY=your-api-key-here
export mysql_pwd=your-mysql-password
```

或在 `src/main/resources/application.yaml` 中配置数据库密码：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_boot?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    username: root
    password: your-mysql-password
    driver-class-name: com.mysql.cj.jdbc.Driver
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

## 架构概览

```
┌─────────────────┐
│   认证拦截层    │  ← JWT身份验证
└────────┬────────┘
         │
┌────────▼────────┐
│  权限校验层     │  ← RBAC权限检查
└────────┬────────┘
         │
┌────────▼────────┐
│  WebSocket 层   │  ← 客户端连接
└────────┬────────┘
         │
┌────────▼────────┐
│  消息路由层     │  ← 自动分发消息
└────────┬────────┘
         │
┌────────▼────────┐
│  业务处理层     │  ← MessageHandler 实现
└────────┬────────┘
         │
┌────────▼────────┐
│   AI 模型层     │  ← 多模型支持
└─────────────────┘
```

### 设计思想

> **业务与技术分离设计**：模块之间通过接口进行调用，模块之间相互独立，互不依赖。

## 核心模块

### 1. Communication Module (通信模块)

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

完整实现参考 [ChatService.java](src/main/java/fun/aiboot/services/ChatService.java)

### 2. Dialogue LLM Module (对话 AI 模块)

提供统一的 AI 模型访问接口，支持多种 AI 服务。

**核心组件**：
- `ChatModelFactory` - 工厂模式创建 AI 模型实例
- `ModelFrameworkType` - 支持的模型类型枚举
- `ToolsGlobalRegistry` - 全局工具注册表
- `GlobalFunction` - 工具调用接口

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

实现 `GlobalFunction` 接口创建自定义工具：

```java
@Component
public class WeatherFunction implements GlobalFunction {
    @Override
    public ToolCallback getFunctionCallTool() {
        return ToolCallback.from(
            "get_weather",
            "获取天气信息",
            this::getWeather
        );
    }

    public String getWeather(String city) {
        return "晴天，25°C";
    }
}
```

工具会自动注册，AI 可在对话中智能调用。

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
    @GetMapping("/function-test")
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
- `ChatService` - 聊天服务，处理对话消息并集成 AI 模型
- `PermissionService` - 权限服务，处理角色和工具权限管理

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
│   ├── annotation/                        # 权限注解
│   ├── aspect/                            # 权限校验切面
│   ├── common/                            # 通用类
│   ├── config/                            # 配置类
│   ├── context/                           # 用户上下文
│   ├── controller/                        # 控制器
│   ├── entity/                            # 实体类
│   ├── exception/                         # 异常处理
│   ├── interceptor/                       # 拦截器
│   ├── mapper/                            # Mapper接口
│   ├── service/                           # 业务服务接口
│   │   └── impl/                          # 业务服务实现
│   ├── communication/                     # 通信模块
│   │   ├── config/                        # WebSocket配置
│   │   ├── domain/                        # 消息实体
│   │   ├── interceptor/                   # WebSocket拦截器
│   │   └── server/                        # WebSocket服务端实现
│   ├── dialogue/llm/                      # AI对话模块
│   │   ├── config/                        # 对话配置
│   │   ├── factory/                       # 模型工厂
│   │   ├── impl/                          # LLM服务实现
│   │   ├── memory/                        # 对话记忆
│   │   ├── providers/                     # 模型提供者
│   │   └── tool/                          # 工具调用
│   └── utils/                             # 工具类
├── src/main/resources/
│   ├── application.yaml                   # 应用配置
│   └── application.properties             # 应用配置（备用）
├── src/test/java/fun/aiboot/              # 测试代码
├── TECHNICAL_DOCUMENTATION.md             # 技术文档
├── SECURITY_IMPLEMENTATION.md             # 安全实现说明
├── README.md                              # 项目说明
└── pom.xml                                # Maven 配置
```

## 文档

- **[技术文档](TECHNICAL_DOCUMENTATION.md)** - 详细的架构设计、API 说明、开发指南
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
- [ ] 多轮对话上下文
- [ ] 集群部署支持
- [ ] 更丰富的权限控制策略
- [ ] 审计日志功能
- [ ] RAG数据库支持
- [ ] 后台管理系统

## 贡献

欢迎贡献代码、报告问题或提出改进建议！

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

**Built with ❤️ using Spring Boot and Spring AI**
