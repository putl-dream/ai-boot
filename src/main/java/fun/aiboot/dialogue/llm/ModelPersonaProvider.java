package fun.aiboot.dialogue.llm;

import fun.aiboot.dialogue.llm.config.ModelConfig;
import org.springframework.ai.chat.messages.SystemMessage;

public interface ModelPersonaProvider {
    SystemMessage getSystemPrompt(ModelConfig model);
}