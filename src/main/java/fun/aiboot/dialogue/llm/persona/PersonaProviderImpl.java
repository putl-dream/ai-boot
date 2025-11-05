package fun.aiboot.dialogue.llm.persona;

import fun.aiboot.dialogue.llm.config.LlmModelConfiguration;
import fun.aiboot.entity.SysPrompt;
import fun.aiboot.service.SysPromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersonaProviderImpl implements PersonaProvider {

    private final SysPromptService sysPromptService;

    @Override
    public SystemMessage getSystemPrompt(LlmModelConfiguration model) {
        String id = model.getId();
        SysPrompt prompt = sysPromptService.getById(id);
        return new SystemMessage(prompt.getSkill());
    }
}
