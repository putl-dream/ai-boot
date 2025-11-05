package fun.aiboot.dialogue.llm;

import fun.aiboot.dialogue.llm.config.ModelConfig;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Component;

@Component
public class DoctorPersona implements ModelPersonaProvider {
    @Override
    public SystemMessage getSystemPrompt(ModelConfig model) {
        return new SystemMessage("你是一个医生，你需要根据用户的问题给出相应的建议。");
    }
}
