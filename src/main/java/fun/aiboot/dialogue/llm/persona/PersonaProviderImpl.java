package fun.aiboot.dialogue.llm.persona;

import fun.aiboot.dialogue.llm.config.LlmModelConfiguration;
import fun.aiboot.entity.ModelRole;
import fun.aiboot.service.ModelRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersonaProviderImpl implements PersonaProvider {

    private final ModelRoleService modelRoleService;

    @Override
    public SystemMessage getSystemPrompt(LlmModelConfiguration model) {
        String id = model.getId();
        ModelRole prompt = modelRoleService.getById(id);
        return new SystemMessage(prompt.getSkill());
    }
}
