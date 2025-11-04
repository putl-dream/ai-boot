package fun.aiboot.service;

import fun.aiboot.entity.Conversation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ConversationServiceTest {

    @Autowired
    private ConversationService conversationService;

    @Test
    void test() {
        conversationService.save(Conversation.builder()
                .userId("1")
                .build());
    }

}