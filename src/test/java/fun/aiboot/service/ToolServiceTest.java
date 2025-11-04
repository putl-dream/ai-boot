package fun.aiboot.service;

import fun.aiboot.entity.Tool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ToolServiceTest {

    @Autowired
    private ToolService toolService;

    @Test
    void testSaveTool() {
        Tool tool = Tool.builder()
                .name("calculator")
                .description("A simple calculator tool")
                .build();
        
        boolean saved = toolService.save(tool);
        assertTrue(saved);
        assertNotNull(tool.getId());
    }

    @Test
    void testGetToolById() {
        // First save a tool
        Tool tool = Tool.builder()
                .name("calculator")
                .description("A simple calculator tool")
                .build();
        toolService.save(tool);
        
        // Then retrieve it
        Tool retrieved = toolService.getById(tool.getId());
        assertNotNull(retrieved);
        assertEquals("calculator", retrieved.getName());
        assertEquals("A simple calculator tool", retrieved.getDescription());
    }
}