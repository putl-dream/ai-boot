package fun.aiboot.dialogue.llm.config;

import fun.aiboot.dialogue.llm.context.DialogueContext;
import fun.aiboot.dialogue.llm.context.MySQLDialogueContext;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    @Resource
    MySQLDialogueContext mySQLChatMemory;

    @Bean
    public DialogueContext chatMemory() {
        return mySQLChatMemory;
    }

}
