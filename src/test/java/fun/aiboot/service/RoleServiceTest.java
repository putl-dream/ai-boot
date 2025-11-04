package fun.aiboot.service;

import fun.aiboot.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RoleServiceTest {
    @Autowired
    private RoleService roleService;

    @Test
    void testSaveRole() {
        Role role = Role.builder()
                .name("admin")
                .description("Administrator role")
                .build();
        
        boolean saved = roleService.save(role);
        assertTrue(saved);
        assertNotNull(role.getId());
    }

    @Test
    void testGetRoleById() {
        // First save a role
        Role role = Role.builder()
                .name("admin")
                .description("Administrator role")
                .build();
        roleService.save(role);
        
        // Then retrieve it
        Role retrieved = roleService.getById(role.getId());
        assertNotNull(retrieved);
        assertEquals("admin", retrieved.getName());
        assertEquals("Administrator role", retrieved.getDescription());
    }
    
    @Test
    void testCreateRole() {
        Role role = Role.builder()
                .name("user")
                .description("User role")
                .build();
        
        assertDoesNotThrow(() -> {
            roleService.createRole(role);
        });
    }
}