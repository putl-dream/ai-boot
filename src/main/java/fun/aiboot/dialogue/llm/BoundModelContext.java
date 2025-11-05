package fun.aiboot.dialogue.llm;

import fun.aiboot.dialogue.llm.config.ModelConfig;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;


public record BoundModelContext(ModelConfig config, List<Message> context) {
    public Prompt buildPrompt(String userMessage) {
        if (userMessage != null) context.addLast(new UserMessage(userMessage));
        return new Prompt(context);
    }
}