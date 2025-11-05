package fun.aiboot.dialogue.llm.persona;

import fun.aiboot.dialogue.llm.config.LlmModelConfiguration;
import org.springframework.ai.chat.messages.SystemMessage;

public interface PersonaProvider {
    SystemMessage getSystemPrompt(LlmModelConfiguration model);
}