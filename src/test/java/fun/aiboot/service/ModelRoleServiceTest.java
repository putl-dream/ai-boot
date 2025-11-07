package fun.aiboot.service;

import fun.aiboot.entity.ModelRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ModelRoleServiceTest {
    @Autowired
    private ModelRoleService modelRoleService;

    @Test
    public void save() {
        ModelRole modelRole = new ModelRole();
        modelRole.setName("doctor");
        modelRole.setDescription("医生");
        modelRole.setSkill("你是一个医生，你需要根据用户的问题给出相应的建议。");

        modelRoleService.save(modelRole);
    }

}