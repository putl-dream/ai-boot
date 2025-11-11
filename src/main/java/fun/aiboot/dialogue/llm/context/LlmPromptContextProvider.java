package fun.aiboot.dialogue.llm.context;

import fun.aiboot.common.exception.AuthorizationException;
import fun.aiboot.dialogue.llm.config.LlmModelConfiguration;
import fun.aiboot.entity.Model;
import fun.aiboot.service.ModelService;
import fun.aiboot.services.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LlmPromptContextProvider {

    private final ModelConfigContext modelConfigContext;
    private final DialogueContext dialogueContext;
    private final PermissionService permissionService;
    private final ModelService modelService;

    /**
     * 组合模型配置与上下文信息，生成可运行环境
     */
    public LlmPromptContext bind(String userId, String modelId) {
        final LlmModelConfiguration llmModelConfiguration = modelConfigContext.get(modelId);

        if (llmModelConfiguration == null) {
            throw new RuntimeException("模型不存在");
        }

        Model model = modelService.getById(modelId);

        if (!permissionService.hasModelIds(userId, List.of(model.getName()), false)) {
            throw new AuthorizationException("无权访问模型");
        }

        return new LlmPromptContext(llmModelConfiguration, dialogueContext.getMessages(userId, 10));
    }
}