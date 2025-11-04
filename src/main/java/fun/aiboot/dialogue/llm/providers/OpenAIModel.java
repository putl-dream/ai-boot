package fun.aiboot.dialogue.llm.providers;

import fun.aiboot.dialogue.llm.tool.ToolsGlobalRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.List;

public class OpenAIModel implements ChatModel {

    private final OpenAiChatModel openAiChatModel;

    public OpenAIModel(String modelName, String apiKey) {
        this(modelName, apiKey, null, null);
    }

    public OpenAIModel(String modelName, String apiKey, ToolCallingManager toolCallingManager, List<ToolCallback> toolCallbacks) {
        Assert.notNull(apiKey, "apiKey must not be null");
        Assert.notNull(modelName, "modelName must not be null");

        if (toolCallingManager == null || toolCallbacks == null || toolCallbacks.isEmpty()) {
            openAiChatModel = OpenAiChatModel.builder()
                    .defaultOptions(OpenAiChatOptions
                            .builder()
                            .model(modelName)
                            .toolCallbacks(toolCallbacks)
                            .build())
                    .openAiApi(OpenAiApi.builder()
                            .apiKey(apiKey)
                            .build())
                    .build();
        } else {
            openAiChatModel = OpenAiChatModel.builder()
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model(modelName)
                            .toolCallbacks(toolCallbacks)
                            .build())
                    .openAiApi(OpenAiApi.builder()
                            .apiKey(apiKey)  // 设置有效的 API 密钥
                            .build())
                    .toolCallingManager(toolCallingManager)
                    .build();
        }
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return openAiChatModel.call(prompt);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return openAiChatModel.stream(prompt);
    }

    public static class Builder {
        private String modelName = "qwen-plus";
        private String apiKey;
        private ToolCallingManager toolCallingManager;
        private ToolsGlobalRegistry toolsGlobalRegistry;

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder toolCallingManager(ToolCallingManager toolCallingManager) {
            this.toolCallingManager = toolCallingManager;
            return this;
        }

        public Builder toolsGlobalRegistry(ToolsGlobalRegistry toolsGlobalRegistry) {
            this.toolsGlobalRegistry = toolsGlobalRegistry;
            return this;
        }

        public DashscopeModel build() {
            List<ToolCallback> toolCallbacks = toolsGlobalRegistry.getAllFunctions().values().stream().toList();
            if (toolCallingManager == null && toolCallbacks.isEmpty()) {
                return new DashscopeModel(modelName, apiKey);
            }
            return new DashscopeModel(modelName, apiKey, toolCallingManager, toolCallbacks);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
