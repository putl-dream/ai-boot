package fun.aiboot.service;

import fun.aiboot.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserRoleServiceTest {
    @Autowired
    private UserRoleService userRoleService;

    @Test
    void test() {
        userRoleService.save(UserRole.builder()
                .userId("1")
                .roleId("1")
                .build());
    }
}