package fun.aiboot.service;

import fun.aiboot.entity.SysPrompt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SysPromptServiceTest {
    @Autowired
    private SysPromptService sysPromptService;

    @Test
    public void save() {
        SysPrompt sysPrompt = new SysPrompt();
        sysPrompt.setName("doctor");
        sysPrompt.setDescription("医生");
        sysPrompt.setSkill("你是一个医生，你需要根据用户的问题给出相应的建议。");

        sysPromptService.save(sysPrompt);
    }

}