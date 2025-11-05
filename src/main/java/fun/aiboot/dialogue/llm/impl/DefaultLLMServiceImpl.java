package fun.aiboot.dialogue.llm.impl;


import fun.aiboot.dialogue.llm.*;
import fun.aiboot.dialogue.llm.factory.ChatModelFactory;
import fun.aiboot.services.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultLLMServiceImpl implements LLMService {

    private final ModelContextBinder modelContextBinder;
    private final PermissionService permissionService;
    private final ModelPersonaProvider modelPersonaProvider;
    private final ToolCallingManager toolCallingManager;

    private BoundModelContext bound;


    @Override
    public String chat(String userId, String modelId, String message) {
        Prompt prompt = buildPrompt(userId, modelId, message);

        ChatModel chatModel = buildModel();

        ChatResponse call = chatModel.call(prompt);
        return call.getResult().getOutput().getText();
    }

    @Override
    public Flux<String> stream(String userId, String modelId, String message) {
        Prompt prompt = buildPrompt(userId, modelId, message);
        ChatModel chatModel = buildModel();

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
                    log.info("Stream completed for user: {}", userId);
                    log.info("Response: {}", responseBuilder);
                })
                .doOnError(error -> log.error("Stream error for user {}: {}", userId, error.getMessage(), error));
    }


    private Prompt buildPrompt(String userId, String modelId, String message) {
        /*
         * 通过模型上下文绑定器得到  绑定上下文
         */
        bound = modelContextBinder.bind(userId, modelId);

        /*
         * 根据上下文校验模型权限
         */

        if (!permissionService.canAccessModel(userId, bound.config())) {
            throw new SecurityException("no access to model");
        }

        /*
         * 添加角色上下文，构建系统提示词
         */
        SystemMessage systemPrompt = modelPersonaProvider.getSystemPrompt(bound.config());
        bound.context().addFirst(systemPrompt);

        /*
         * 将用户消息添加到上下文中
         */
        UserMessage userMessage = new UserMessage(message);
        bound.context().addLast(userMessage);

        return new Prompt(bound.context());
    }

    private ChatModel buildModel() {
        return ChatModelFactory.builder()
                .modelConfig(bound.config())
                .toolCallingManager(toolCallingManager)
                .build()
                .takeChatModel();
    }
}
