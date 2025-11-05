package fun.aiboot.dialogue.llm.factory;

import fun.aiboot.dialogue.llm.config.ModelConfig;
import fun.aiboot.dialogue.llm.providers.DashscopeModel;
import fun.aiboot.dialogue.llm.providers.OpenAIModel;
import lombok.Builder;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

@Builder
public class ChatModelFactory {

    private ModelConfig modelConfig;
    private ToolCallingManager toolCallingManager;

    // 使用工厂模式创建模型实例，预留可扩展
    public ChatModel takeChatModel() {
        return switch (modelConfig.getProvider()) {
            case "openai" -> OpenAIModel.builder()
                    .withApiKey(modelConfig.getApiKey())
                    .withModel(modelConfig.getModelName())
                    .withToolCallingManager(toolCallingManager)
                    .withTools(mapToolCallback(modelConfig.getExposedTools()))
                    .build();
            case "dashscope" -> DashscopeModel.builder()
                    .withApiKey(modelConfig.getApiKey())
                    .withModel(modelConfig.getModelName())
                    .withToolCallingManager(toolCallingManager)
                    .withTools(mapToolCallback(modelConfig.getExposedTools()))
                    .build();
            default -> null;
        };
    }

    private static List<ToolCallback> mapToolCallback(List<String> exposedTools) {
        return null;
    }
}
