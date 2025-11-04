package fun.aiboot.service;

import fun.aiboot.entity.RoleTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RoleToolServiceTest {
    @Autowired
    private RoleToolService roleToolService;

    @Test
    void testSaveRoleTool() {
        RoleTool roleTool = RoleTool.builder()
                .roleId("1")
                .toolId("1")
                .build();
        
        boolean saved = roleToolService.save(roleTool);
        assertTrue(saved);
        assertNotNull(roleTool.getId());
    }

    @Test
    void testGetRoleToolById() {
        // First save a roleTool
        RoleTool roleTool = RoleTool.builder()
                .roleId("1")
                .toolId("1")
                .build();
        roleToolService.save(roleTool);
        
        // Then retrieve it
        RoleTool retrieved = roleToolService.getById(roleTool.getId());
        assertNotNull(retrieved);
        assertEquals("1", retrieved.getRoleId());
        assertEquals("1", retrieved.getToolId());
    }
}