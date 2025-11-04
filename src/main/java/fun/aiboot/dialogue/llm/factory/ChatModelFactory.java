package fun.aiboot.dialogue.llm.factory;

import fun.aiboot.dialogue.llm.providers.DashscopeModel;
import fun.aiboot.dialogue.llm.providers.OpenAIModel;
import fun.aiboot.dialogue.llm.tool.ToolsGlobalRegistry;
import lombok.Builder;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;

@Builder
public class ChatModelFactory {

    private ToolCallingManager toolCallingManager;
    private ToolsGlobalRegistry toolsGlobalRegistry;
    private String apiKey;
    private String modelName;

    // 使用工厂模式创建模型实例，预留可扩展
    public ChatModel takeChatModel(ModelFrameworkType modelFrameworkType) {
        return switch (modelFrameworkType) {
            case openai -> OpenAIModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .toolCallingManager(toolCallingManager)
                    .toolsGlobalRegistry(toolsGlobalRegistry)
                    .build();
            case dashscope -> DashscopeModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .toolCallingManager(toolCallingManager)
                    .toolsGlobalRegistry(toolsGlobalRegistry)
                    .build();
            default -> null;
        };
    }
}
