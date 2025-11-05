package fun.aiboot.dialogue.llm;

import fun.aiboot.dialogue.llm.config.ModelConfig;
import fun.aiboot.dialogue.llm.memory.ChatMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModelContextBinder {

    private final ModelConfigManager modelConfigManager;
    private final ChatMemory chatMemory;

    /**
     * 组合模型配置与上下文信息，生成可运行环境
     */
    public BoundModelContext bind(String userId, String modelId) {
        ModelConfig modelConfig = modelConfigManager.get(modelId);

        return new BoundModelContext(modelConfig, chatMemory.getMessages(userId, 10));
    }
}