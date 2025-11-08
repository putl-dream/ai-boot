package fun.aiboot.dialogue.llm.model;

import fun.aiboot.dialogue.llm.config.LlmModelConfiguration;
import fun.aiboot.dialogue.llm.providers.DashscopeLlmProvider;
import fun.aiboot.dialogue.llm.providers.OpenAiLlmProvider;
import lombok.Builder;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

@Builder
public class ChatModelFactory {

    private LlmModelConfiguration llmModelConfiguration;
    private ToolCallingManager toolCallingManager;
    private List<ToolCallback> toolCallbacks;

    // 使用工厂模式创建模型实例，预留可扩展
    public ChatModel takeChatModel() {
        return switch (llmModelConfiguration.getProvider()) {
            case "openai" -> OpenAiLlmProvider.builder()
                    .withApiKey(llmModelConfiguration.getApiKey())
                    .withModel(llmModelConfiguration.getModelName())
                    .withToolCallingManager(toolCallingManager)
                    .withTools(toolCallbacks)
                    .build();
            case "dashscope" -> DashscopeLlmProvider.builder()
                    .withApiKey(llmModelConfiguration.getApiKey())
                    .withModel(llmModelConfiguration.getModelName())
                    .withToolCallingManager(toolCallingManager)
                    .withTools(toolCallbacks)
                    .build();
            default -> null;
        };
    }
}
