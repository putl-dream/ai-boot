package fun.aiboot.dialogue.llm.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import fun.aiboot.communication.server.SessionManager;
import fun.aiboot.context.UserContext;
import fun.aiboot.context.UserContextHolder;
import fun.aiboot.dialogue.llm.LLMService;
import fun.aiboot.dialogue.llm.factory.ChatModelFactory;
import fun.aiboot.dialogue.llm.factory.ModelFrameworkType;
import fun.aiboot.dialogue.llm.memory.ChatMemory;
import fun.aiboot.dialogue.llm.tool.ToolsGlobalRegistry;
import fun.aiboot.entity.Model;
import fun.aiboot.service.ModelService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DefaultLLMServiceImpl implements LLMService {
    private final ChatMemory chatMemory;
    private final ToolsGlobalRegistry toolsGlobalRegistry;
    private final ModelService modelService;
    private final ToolCallingManager toolCallingManager;
    Map<String, ChatModel> chatModelMap = new ConcurrentHashMap<>();
    private final SessionManager sessionManager;


    /**
     * 初始化模型
     */
    @PostConstruct
    public void init() {
        List<Model> models = modelService.getBaseMapper().selectList(Wrappers.lambdaQuery(Model.class));
        for (Model model : models) {
            ChatModel chatModel = ChatModelFactory.builder()
                    .modelName(model.getName())
                    .toolCallingManager(toolCallingManager)
                    .toolsGlobalRegistry(toolsGlobalRegistry)
                    .apiKey(model.getModelKey())
                    .build()
                    .takeChatModel(ModelFrameworkType.dashscope);
            chatModelMap.put(model.getId(), chatModel);
            log.info("{} 初始化模型成功", model.getName());
        }
    }


    public DefaultLLMServiceImpl(ToolCallingManager toolCallingManager,
                                 ToolsGlobalRegistry toolsGlobalRegistry,
                                 ChatMemory chatMemory,
                                 ModelService modelService,
                                 SessionManager sessionManager
    ) {
        this.toolsGlobalRegistry = toolsGlobalRegistry;
        this.chatMemory = chatMemory;
        this.modelService = modelService;
        this.toolCallingManager = toolCallingManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public String chat(String userId, String message) {
        WebSocketSession session = sessionManager.getSession(userId);
        UserContext userContext = (UserContext) session.getAttributes().get("userContext");

        Prompt prompt = buildPrompt(userId, message);

        ChatResponse call = chatModelMap.get(userContext.getCurrentModelId()).call(prompt);
        String text = call.getResult().getOutput().getText();
        text = StringUtils.isBlank(text) ? "" : text;

        addHistory(userId, message, text);
        return text;
    }

    @Override
    public Flux<String> stream(String userId, String message) {
        WebSocketSession session = sessionManager.getSession(userId);
        UserContext userContext = (UserContext) session.getAttributes().get("userContext");

        Prompt prompt = buildPrompt(userContext.getUserId(), message);

        // 使用 StringBuilder 收集完整响应
        StringBuilder completeResponse = new StringBuilder();

        return chatModelMap.get(userContext.getCurrentModelId()).stream(prompt)
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

    public Map<String, ToolCallback> getAllFunctions() {
        return toolsGlobalRegistry.getAllFunctions();
    }

    private Prompt buildPrompt(String userId, String message) {
        List<Message> messages = chatMemory.getMessages(userId, 10);
        messages.add(new UserMessage(message));
        return new Prompt(messages);
    }

    private void addHistory(String userId, String userMsg, String assistantMsg) {
        UserMessage userMessage = new UserMessage(userMsg);
        AssistantMessage assistantMessage = new AssistantMessage(assistantMsg);
        chatMemory.addMessage(userId, userMessage);
        chatMemory.addMessage(userId, assistantMessage);
    }
}
