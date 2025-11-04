package fun.aiboot.service;

import fun.aiboot.entity.RoleModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RoleModelServiceTest {

    @Autowired
    private RoleModelService roleModelService;

    @Test
    void testSaveRoleModel() {
        RoleModel roleModel = RoleModel.builder()
                .roleId("role1")
                .modelId("model1")
                .build();
        
        boolean saved = roleModelService.save(roleModel);
        assertTrue(saved);
        assertNotNull(roleModel.getId());
    }

    @Test
    void testGetRoleModelById() {
        // First save a roleModel
        RoleModel roleModel = RoleModel.builder()
                .roleId("role1")
                .modelId("model1")
                .build();
        roleModelService.save(roleModel);
        
        // Then retrieve it
        RoleModel retrieved = roleModelService.getById(roleModel.getId());
        assertNotNull(retrieved);
        assertEquals("role1", retrieved.getRoleId());
        assertEquals("model1", retrieved.getModelId());
    }
}