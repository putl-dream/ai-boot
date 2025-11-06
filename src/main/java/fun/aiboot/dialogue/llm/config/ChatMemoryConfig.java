package fun.aiboot.dialogue.llm.config;

import fun.aiboot.websocket.server.SessionManager;
import fun.aiboot.dialogue.llm.context.DialogueContext;
import fun.aiboot.dialogue.llm.context.MySQLDialogueContext;
import fun.aiboot.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatMemoryConfig {

    private final MessageMapper messageMapper;
    private final SessionManager sessionManager;

    @Bean
    public DialogueContext chatMemory() {
        return new MySQLDialogueContext(messageMapper, sessionManager);
    }

}
