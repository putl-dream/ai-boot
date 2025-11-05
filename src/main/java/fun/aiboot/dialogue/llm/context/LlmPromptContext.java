package fun.aiboot.dialogue.llm.context;

import fun.aiboot.dialogue.llm.config.LlmModelConfiguration;
import org.springframework.ai.chat.messages.Message;

import java.util.List;


public record LlmPromptContext(LlmModelConfiguration config, List<Message> context) {
}