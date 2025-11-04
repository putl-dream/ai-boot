package fun.aiboot.service;

import fun.aiboot.entity.RoleTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RoleToolServiceTest {
    @Autowired
    private RoleToolService roleToolService;

    @Test
    void test() {
        roleToolService.save(RoleTool.builder()
                .roleId("1")
                .toolId("1")
                .build());
    }
}