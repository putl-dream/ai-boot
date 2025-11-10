package fun.aiboot.common.initialize;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import fun.aiboot.dialogue.llm.tool.GlobalToolRegistry;
import fun.aiboot.entity.*;
import fun.aiboot.service.*;
import fun.aiboot.services.AuthService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MySQLInit {

    private final UserService userService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final ModelService modelService;
    private final ModelRoleService modelRoleService;
    private final ToolService toolService;
    private final RoleModelService roleModelService;
    private final RoleToolService roleToolService;
    private final GlobalToolRegistry globalToolRegistry;
    private final ConversationService conversationService;
    private final AuthService authService;
    private final String modelName = "Moonshot-Kimi-K2-Instruct";


    @PostConstruct
    public void init() {
//        log.info("=================================[ 数据库初始化开始 ]==========================================");
        // 全局工具初始化
        Map<String, Tool> allPermission = globalToolRegistry.getAllPermission();
        allPermission.values().forEach(tool -> {
            String name = tool.getName();
            if (toolService.isEmpty(name)) {
                toolService.save(tool);
                log.info("[ 新增工具 ] tool {} 添加成功", name);
            }
        });
        log.info("[ 模型工具初始化成功 ]");

        // 角色初始化
        long count = roleService.count(Wrappers.lambdaQuery(Role.class));
        if (count == 0) {
            roleService.save(Role.builder()
                    .name("USER")
                    .description("普通用户")
                    .build());
            roleService.save(Role.builder()
                    .name("ADMIN")
                    .description("管理员")
                    .build());
        }
        log.info("[ 用户角色初始化成功 ]");

        // 模型初始化
        count = modelService.count(Wrappers.lambdaQuery(Model.class));
        if (count == 0) {
            modelService.save(Model.builder()
                            .name(modelName)
                            .provider("dashscope")
                    .modelKey("请更换成自己的 api-key")
                            .maxTokens(1024)
                            .description("kimi k2 模型")
                            .build()
            );
        }
        log.info("[ 模型初始化成功 ]");

        // 模型角色初始化

        count = modelRoleService.count(Wrappers.lambdaQuery(ModelRole.class).eq(ModelRole::getName, "ai"));
        if (count == 0) {
            modelRoleService.save(ModelRole.builder()
                    .name("ai")
                    .description("通用角色")
                    .skill("""
                            你是一个专业、严谨、乐于助人的AI助手，具备多领域知识和优秀的逻辑推理能力。你的目标是：
                            1. **准确理解用户意图**：仔细阅读用户输入，识别核心需求、背景信息和隐含期望。
                            2. **提供高质量输出**：
                               - 结构清晰、逻辑严密、语言简洁自然。
                               - 优先使用分点、编号、表格、代码块等格式提升可读性。
                               - 若涉及计算、推理或代码，确保结果正确、可验证。
                            3. **保持中立与客观**：不偏向任何一方，不输出未经证实的主观判断。
                            4. **安全与合规**：拒绝生成违法、有害、歧视性或误导性内容。
                            5. **灵活应变**：
                               - 如果信息不足，主动询问澄清。
                               - 如果问题模糊，尝试提供多种合理方案并说明适用场景。
                               - 支持多语言输入，输出语言与用户一致（除非特别要求）。
                            """)
                    .build());
        }
        log.info("[ 模型角色初始化成功 ]");


        // 用户初始化
        count = userService.count(Wrappers.lambdaQuery(User.class).in(User::getUsername, "admin", "user"));
        if (count == 0) {
            authService.register("admin", "123456", "default", "ai");
            authService.register("user", "123456", "default", "ai");
        } else {
            User admin = userService.getByUserName("admin");
            if (admin == null) {
                authService.register("admin", "123456", "default", "ai");
            }
            User user = userService.getByUserName("user");
            if (user == null) {
                authService.register("user", "123456", "default", "ai");
            }
        }
        log.info("[ 用户初始化成功 ]");

        // 为用户创建角色
        count = userRoleService.count(Wrappers.lambdaQuery(UserRole.class));
        Role roleAdmin = roleService.getByName("ADMIN");
        Role roleUser = roleService.getByName("USER");
        if (count == 0) {
            User admin = userService.getByUserName("admin");

            userRoleService.save(UserRole.builder()
                    .userId(admin.getId())
                    .roleId(roleAdmin.getId())
                    .build());
            User user = userService.getByUserName("user");
            userRoleService.save(UserRole.builder()
                    .userId(user.getId())
                    .roleId(roleUser.getId())
                    .build());
        }
        log.info("[ 用户角色关系初始化成功 ]");

        // 为角色添加模型
        count = roleModelService.count(Wrappers.lambdaQuery(RoleModel.class));
        if (count == 0) {
            Model byName = modelService.getByName(modelName);
            roleModelService.save(RoleModel.builder()
                    .roleId(roleAdmin.getId())
                    .modelId(byName.getId())
                    .build());
            roleModelService.save(RoleModel.builder()
                    .roleId(roleUser.getId())
                    .modelId(byName.getId())
                    .build());
        }
        log.info("[ 角色模型关系初始化成功 ]");

        // 为管理员添加工具
        count = roleToolService.count(Wrappers.lambdaQuery(RoleTool.class));
        if (count == 0) {
            for (Tool tool : toolService.getBaseMapper().selectList(Wrappers.lambdaQuery(Tool.class))) {
                Role byId = roleService.getByName("ADMIN");
                roleToolService.save(RoleTool.builder()
                        .roleId(byId.getId())
                        .toolId(tool.getId())
                        .build()
                );
            }
        }
        log.info("[ 角色工具关系初始化成功 ]");

        // 测试：创建一个默认会话
        count = conversationService.count(Wrappers.lambdaQuery(Conversation.class));
        if (count == 0) {
            User admin = userService.getByUserName("admin");
            Conversation conversation = Conversation.builder()
                    .userId(admin.getId())
                    .title("初始化测试会话")
                    .modelId(modelName)
                    .build();
            conversationService.save(conversation);
        }
        log.info("[ 工具初始化成功 ]");
        log.info("=================================[ 数据库初始化成功 ]==========================================");
    }
}
