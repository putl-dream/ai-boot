package fun.aiboot.service;

import fun.aiboot.entity.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MessageServiceTest {
    @Autowired
    private MessageService messageService;

    @Test
    void testSaveMessage() {
        Message message = Message.builder()
                .content("hello")
                .conversationId("1")
                .sender("user")
                .build();
        
        boolean saved = messageService.save(message);
        assertTrue(saved);
        assertNotNull(message.getId());
    }

    @Test
    void testGetMessageById() {
        // First save a message
        Message message = Message.builder()
                .content("hello")
                .conversationId("1")
                .sender("user")
                .build();
        messageService.save(message);
        
        // Then retrieve it
        Message retrieved = messageService.getById(message.getId());
        assertNotNull(retrieved);
        assertEquals("hello", retrieved.getContent());
    }
}