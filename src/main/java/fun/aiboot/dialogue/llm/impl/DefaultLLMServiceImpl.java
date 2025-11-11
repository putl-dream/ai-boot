package fun.aiboot.dialogue.llm.impl;


import fun.aiboot.dialogue.llm.LLMService;
import fun.aiboot.dialogue.llm.context.DialogueContext;
import fun.aiboot.dialogue.llm.context.LlmPromptContext;
import fun.aiboot.dialogue.llm.context.LlmPromptContextProvider;
import fun.aiboot.dialogue.llm.model.ChatModelFactory;
import fun.aiboot.dialogue.llm.persona.PersonaProvider;
import fun.aiboot.dialogue.llm.tool.GlobalToolRegistry;
import fun.aiboot.services.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultLLMServiceImpl implements LLMService {

    private final LlmPromptContextProvider llmPromptContextProvider;
    private final PermissionService permissionService;
    private final PersonaProvider personaProvider;
    private final ToolCallingManager toolCallingManager;
    private final GlobalToolRegistry globalToolRegistry;
    private final DialogueContext dialogueContext;

    private LlmPromptContext bound;


    @Override
    public String chat(String userId, String modelId, String message) {
        Prompt prompt = buildPrompt(userId, modelId, message);
        List<ToolCallback> toolCallbacks = buildToolCallbacks(userId);
        ChatModel chatModel = buildModel(toolCallbacks);

        ChatResponse call = chatModel.call(prompt);
        return call.getResult().getOutput().getText();
    }

    @Override
    public Flux<String> stream(String userId, String modelId, String message) {
        dialogueContext.addMessage(userId, new UserMessage(message));

        Prompt prompt = buildPrompt(userId, modelId, message);
        List<ToolCallback> toolCallbacks = buildToolCallbacks(userId);
        ChatModel chatModel = buildModel(toolCallbacks);

        // 全量内容
        StringBuilder responseBuilder = new StringBuilder();

        return chatModel.stream(prompt)
                .mapNotNull(chatResponse -> {
                    if (chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                        String chunk = chatResponse.getResult().getOutput().getText();
                        if (chunk != null && !chunk.isEmpty()) {
                            responseBuilder.append(chunk);
                            return chunk; // ✅ 返回内容
                        }
                    }
                    return null; // ✅ 没有内容就返回 null
                })
                .doOnNext(token -> log.debug("Streaming token: {}", token))
                .doOnComplete(() -> {
                    log.info("[ 模型消息 ] 角色：{}，模型：{} : \n{}\n", bound.config().getRoleName(), bound.config().getModelName(), responseBuilder);
                    dialogueContext.addMessage(userId, new AssistantMessage(responseBuilder.toString()));
                })
                .doOnError(error -> log.error("Stream error for user {}: {}", userId, error.getMessage(), error));
    }


    private Prompt buildPrompt(String userId, String modelId, String message) {
        log.info("[ 用户消息 ] 用户 {} : \n{}\n", userId, message);
        /*
         * 通过模型上下文绑定器得到  绑定上下文
         */
        bound = llmPromptContextProvider.bind(userId, modelId);

        /*
         * 添加角色上下文，构建系统提示词
         */
        SystemMessage systemPrompt = personaProvider.getSystemPrompt(bound.config());
        bound.context().addFirst(systemPrompt);

        /*
         * 将用户消息添加到上下文中
         */
        UserMessage userMessage = new UserMessage(message);
        bound.context().addLast(userMessage);

        return new Prompt(bound.context());
    }

    private List<ToolCallback> buildToolCallbacks(String userId) {
        List<String> userTools = permissionService.getUserTools(userId);
        log.info("[ 用户工具 ] 用户 {} : \n{}\n", userId, userTools);

        Map<String, ToolCallback> toolsByNames = globalToolRegistry.getToolsByNames(userTools);
        return toolsByNames.values().stream().toList();
    }


    private ChatModel buildModel(List<ToolCallback> toolCallbacks) {
        return ChatModelFactory.builder()
                .llmModelConfiguration(bound.config())
                .toolCallbacks(toolCallbacks)
                .toolCallingManager(toolCallingManager)
                .build()
                .takeChatModel();
    }
}
