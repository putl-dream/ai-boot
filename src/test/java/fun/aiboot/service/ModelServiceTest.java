package fun.aiboot.service;

import fun.aiboot.entity.Model;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ModelServiceTest {

    @Autowired
    private ModelService modelService;

    @Test
    void testSaveModel() {
        Model model = Model.builder()
                .name("qwen-plus")
                .provider("dashscope")
                .modelKey("sk-dashscope-qwen-plus-2023-10-01")
                .maxTokens(1024)
                .description("qwen-plus")
                .build();
        
        boolean saved = modelService.save(model);
        assertTrue(saved);
        assertNotNull(model.getId());
    }

    @Test
    void testGetModelById() {
        // First save a model
        Model model = Model.builder()
                .name("qwen-plus")
                .provider("dashscope")
                .modelKey("sk-dashscope-qwen-plus-2023-10-01")
                .maxTokens(1024)
                .description("qwen-plus")
                .build();
        modelService.save(model);
        
        // Then retrieve it
        Model retrieved = modelService.getById(model.getId());
        assertNotNull(retrieved);
        assertEquals("qwen-plus", retrieved.getName());
        assertEquals("dashscope", retrieved.getProvider());
    }
}