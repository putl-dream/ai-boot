package fun.aiboot.service;

import fun.aiboot.entity.Conversation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ConversationServiceTest {

    @Autowired
    private ConversationService conversationService;

    @Test
    void testSaveConversation() {
        Conversation conversation = Conversation.builder()
                .userId("1")
                .title("Test Conversation")
                .build();
        
        boolean saved = conversationService.save(conversation);
        assertTrue(saved);
        assertNotNull(conversation.getId());
    }

    @Test
    void testGetConversationById() {
        // First save a conversation
        Conversation conversation = Conversation.builder()
                .userId("1")
                .title("Test Conversation")
                .build();
        conversationService.save(conversation);
        
        // Then retrieve it
        Conversation retrieved = conversationService.getById(conversation.getId());
        assertNotNull(retrieved);
        assertEquals("Test Conversation", retrieved.getTitle());
    }
}