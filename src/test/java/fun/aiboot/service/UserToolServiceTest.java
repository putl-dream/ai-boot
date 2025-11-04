package fun.aiboot.service;

import fun.aiboot.entity.UserTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserToolServiceTest {
    @Autowired
    private UserToolService userToolService;

    @Test
    void testSaveUserTool() {
        UserTool userTool = UserTool.builder()
                .userId("1")
                .toolId("1")
                .build();
        
        boolean saved = userToolService.save(userTool);
        assertTrue(saved);
        assertNotNull(userTool.getId());
    }

    @Test
    void testGetUserToolById() {
        // First save a userTool
        UserTool userTool = UserTool.builder()
                .userId("1")
                .toolId("1")
                .build();
        userToolService.save(userTool);
        
        // Then retrieve it
        UserTool retrieved = userToolService.getById(userTool.getId());
        assertNotNull(retrieved);
        assertEquals("1", retrieved.getUserId());
        assertEquals("1", retrieved.getToolId());
    }
}