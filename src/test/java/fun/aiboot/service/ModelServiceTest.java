package fun.aiboot.service;

import fun.aiboot.entity.Model;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ModelServiceTest {

    @Autowired
    private ModelService modelService;

    @Test
    void test() {
        modelService.save(Model.builder()
                        .name("qwen-plus")
                        .provider("dashscope")
                        .modelKey("sk-dashscope-qwen-plus-2023-10-01")
                        .maxTokens(1024)
                        .description("qwen-plus")
                .build());
    }

}
