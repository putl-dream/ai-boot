package fun.aiboot.dialogue.llm.impl;

import fun.aiboot.communication.server.MessagePublisher;
import fun.aiboot.communication.server.SessionManager;
import fun.aiboot.communication.server.WebSocketConstants;
import fun.aiboot.context.UserContext;
import fun.aiboot.dialogue.llm.LLMService;
import fun.aiboot.dialogue.llm.factory.ChatModelFactory;
import fun.aiboot.dialogue.llm.factory.ModelFrameworkType;
import fun.aiboot.dialogue.llm.memory.ChatMemory;
import fun.aiboot.dialogue.llm.tool.ToolsGlobalRegistry;
import fun.aiboot.entity.Model;
import fun.aiboot.entity.User;
import fun.aiboot.exception.AuthenticationException;
import fun.aiboot.service.ModelService;
import fun.aiboot.service.UserService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultLLMServiceImpl implements LLMService {
    private final ChatMemory chatMemory;
    private final ToolsGlobalRegistry toolsGlobalRegistry;
    private final ModelService modelService;
    private final ToolCallingManager toolCallingManager;
    private final SessionManager sessionManager;
    private final UserService userService;
    private final MessagePublisher messagePublisher;

    private final Map<String, ChatModel> chatModelMap = new ConcurrentHashMap<>();

    private ChatModel initModel(Model model) {
        ChatModel chatModel = ChatModelFactory.builder()
                .modelName(model.getName())
                .toolCallingManager(toolCallingManager)
                .toolsGlobalRegistry(toolsGlobalRegistry)
                .apiKey(model.getModelKey())
                .build()
                .takeChatModel(ModelFrameworkType.dashscope);
        log.info("{} Model create success", model.getName());
        return chatModel;
    }

    @Override
    public String chat(String userId, String message) {
        Assert.notNull(userId, "userId cannot be null");
        Assert.notNull(message, "message cannot be null");
        ChatModel chatModel = getChatModel(userId);

        Prompt prompt = buildPrompt(userId, message);

        ChatResponse call = chatModel.call(prompt);
        String text = call.getResult().getOutput().getText();
        text = StringUtils.isBlank(text) ? "" : text;

        addHistory(userId, message, text);
        return text;
    }

    @Override
    public Flux<String> stream(String userId, String message) {
        Assert.notNull(userId, "userId cannot be null");
        Assert.notNull(message, "message cannot be null");

        ChatModel chatModel = getChatModel(userId);
        Prompt prompt = buildPrompt(userId, message);

        // 使用 StringBuilder 收集完整响应
        StringBuilder completeResponse = new StringBuilder();

        return chatModel.stream(prompt)
                .map(chatResponse -> {
                    // 提取当前片段的文本
                    String chunk = chatResponse.getResult().getOutput().getText();
                    chunk = StringUtils.isBlank(chunk) ? "" : chunk;
                    // 收集到完整响应中
                    completeResponse.append(chunk);
                    return chunk;
                })
                .doOnComplete(() -> {
                    // 流完成后保存历史记录
                    addHistory(userId, message, completeResponse.toString());
                });
    }

    @Override
    public void refreshModel(String userId) {
        Assert.notNull(userId, "userId cannot be null");
        WebSocketSession session = sessionManager.getSession(userId);
        UserContext userContext = (UserContext) session.getAttributes().get(WebSocketConstants.User_Context);

        User user = userService.getById(userId);
        String modelId = user.getModelId();
        chatModelMap.remove(modelId);

        if (!userContext.getRoleModelIds().contains(modelId)) {
            messagePublisher.sendToUser(userId, "您当前暂无该模型权限");
            throw new AuthenticationException("用户没有权限使用此模型");
        }

        Model model = modelService.getById(modelId);
        chatModelMap.put(modelId, initModel(model));
        log.info("{} 刷新模型成功", model.getName());
    }

    private Prompt buildPrompt(String userId, String message) {
        List<Message> messages = chatMemory.getMessages(userId, 10);
        messages.addFirst(new SystemMessage(
                "你是一位专业、耐心且富有同理心的医生。你的职责是根据用户描述的症状、病史或其他健康相关问题，提供科学、准确、易懂的医学建议。" +
                        "请始终遵循以下原则：\n" +
                        "1. 不做确诊，仅提供一般性健康信息和建议；\n" +
                        "2. 若症状严重或紧急，务必建议用户立即就医或拨打急救电话；\n" +
                        "3. 避免使用过于专业的术语，如必须使用，请用通俗语言解释；\n" +
                        "4. 尊重用户隐私，不追问不必要的个人信息；\n" +
                        "5. 强调你的建议不能替代面对面诊疗，鼓励用户咨询正规医疗机构。\n" +
                        "请以温和、关怀的语气与用户交流。"
        ));
        messages.add(new UserMessage(message));
        return new Prompt(messages);
    }

    private void addHistory(String userId, String userMsg, String assistantMsg) {
        UserMessage userMessage = new UserMessage(userMsg);
        AssistantMessage assistantMessage = new AssistantMessage(assistantMsg);
        chatMemory.addMessage(userId, userMessage);
        chatMemory.addMessage(userId, assistantMessage);
    }

    private ChatModel getChatModel(String userId) {
        WebSocketSession session = sessionManager.getSession(userId);
        Assert.notNull(session, "session cannot be null" + userId);
        UserContext userContext = (UserContext) session.getAttributes().get(WebSocketConstants.User_Context);
        Assert.notNull(userContext, "userContext cannot be null");

        User user = userService.getById(userId);
        String modelId = user.getModelId();

        if (!userContext.getRoleModelIds().contains(modelId)) {
            messagePublisher.sendToUser(userId, "您当前暂无该模型权限");
            throw new AuthenticationException("用户没有权限使用此模型");
        }

        return chatModelMap.computeIfAbsent(modelId, id -> {
            Model model = modelService.getById(modelId);
            return initModel(model);
        });
    }
}
