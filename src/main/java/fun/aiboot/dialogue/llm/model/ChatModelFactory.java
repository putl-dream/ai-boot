package fun.aiboot.dialogue.llm.model;

import fun.aiboot.dialogue.llm.config.LlmModelConfiguration;
import fun.aiboot.dialogue.llm.function.ToolsGlobalRegistry;
import fun.aiboot.dialogue.llm.providers.DashscopeLlmProvider;
import fun.aiboot.dialogue.llm.providers.OpenAiLlmProvider;
import lombok.Builder;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Builder
public class ChatModelFactory {

    private LlmModelConfiguration llmModelConfiguration;
    private ToolCallingManager toolCallingManager;
    private ToolsGlobalRegistry toolsGlobalRegistry;

    // 使用工厂模式创建模型实例，预留可扩展
    public ChatModel takeChatModel() {
        return switch (llmModelConfiguration.getProvider()) {
            case "openai" -> OpenAiLlmProvider.builder()
                    .withApiKey(llmModelConfiguration.getApiKey())
                    .withModel(llmModelConfiguration.getModelName())
                    .withToolCallingManager(toolCallingManager)
                    .withTools(mapToolCallback(llmModelConfiguration.getExposedTools()))
                    .build();
            case "dashscope" -> DashscopeLlmProvider.builder()
                    .withApiKey(llmModelConfiguration.getApiKey())
                    .withModel(llmModelConfiguration.getModelName())
                    .withToolCallingManager(toolCallingManager)
                    .withTools(mapToolCallback(llmModelConfiguration.getExposedTools()))
                    .build();
            default -> null;
        };
    }

    // 获取所有工具
    private List<ToolCallback> mapToolCallback(List<String> exposedTools) {
        if (null == exposedTools || exposedTools.isEmpty()) return null;

        Map<String, ToolCallback> allFunctions = toolsGlobalRegistry.getAllFunctions();
        return exposedTools.stream()
                .map(allFunctions::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
