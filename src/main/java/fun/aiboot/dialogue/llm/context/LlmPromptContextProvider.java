package fun.aiboot.dialogue.llm.context;

import fun.aiboot.dialogue.llm.config.LlmModelConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LlmPromptContextProvider {

    private final ModelConfigContext modelConfigContext;
    private final DialogueContext dialogueContext;

    /**
     * 组合模型配置与上下文信息，生成可运行环境
     */
    public LlmPromptContext bind(String userId, String modelId) {
        final LlmModelConfiguration llmModelConfiguration = modelConfigContext.get(modelId);

        // todo 权限校验 ，移动到这里

        return new LlmPromptContext(llmModelConfiguration, dialogueContext.getMessages(userId, 10));
    }
}