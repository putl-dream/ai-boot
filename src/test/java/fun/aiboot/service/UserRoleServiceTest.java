package fun.aiboot.service;

import fun.aiboot.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserRoleServiceTest {
    @Autowired
    private UserRoleService userRoleService;

    @Test
    void testSaveUserRole() {
        UserRole userRole = UserRole.builder()
                .userId("1")
                .roleId("1")
                .build();
        
        boolean saved = userRoleService.save(userRole);
        assertTrue(saved);
        assertNotNull(userRole.getId());
    }

    @Test
    void testGetUserRoleById() {
        // First save a userRole
        UserRole userRole = UserRole.builder()
                .userId("1")
                .roleId("1")
                .build();
        userRoleService.save(userRole);
        
        // Then retrieve it
        UserRole retrieved = userRoleService.getById(userRole.getId());
        assertNotNull(retrieved);
        assertEquals("1", retrieved.getUserId());
        assertEquals("1", retrieved.getRoleId());
    }
}