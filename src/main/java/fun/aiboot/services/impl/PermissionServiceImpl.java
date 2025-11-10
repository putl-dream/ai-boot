package fun.aiboot.services.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import fun.aiboot.dialogue.llm.config.LlmModelConfiguration;
import fun.aiboot.entity.*;
import fun.aiboot.mapper.RoleToolMapper;
import fun.aiboot.mapper.ToolMapper;
import fun.aiboot.mapper.UserToolMapper;
import fun.aiboot.service.*;
import fun.aiboot.services.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 *
 * @author putl
 * @since 2025-10-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final UserRoleService userRoleService;
    private final RoleService roleService;
    private final UserToolMapper userToolMapper;
    private final ToolMapper toolMapper;
    private final RoleToolMapper roleToolMapper;
    private final ModelService modelService;
    private final RoleModelService roleModelService;
    private final UserService userService;
    private final ModelRoleService modelRoleService;

    @Override
    public void createDefaultRole(String userId) {
        // 为用户创建默认角色和工具权限
        Role defaultRole = roleService.getByName("USER");
        userRoleService.save(UserRole.builder()
                .userId(userId)
                .roleId(defaultRole.getId())
                .build());
    }

    @Override
    public boolean hasRole(String userId, List<String> roleNames, boolean requireAll) {
        if (roleNames == null || roleNames.isEmpty()) {
            return true;
        }

        List<String> userRoles = getRoleNames(userId);
        Set<String> userRoleSet = new HashSet<>(userRoles);

        if (requireAll) {
            // 需要拥有所有角色
            return userRoleSet.containsAll(roleNames);
        } else {
            // 只需拥有其中一个角色
            return roleNames.stream().anyMatch(userRoleSet::contains);
        }
    }

    @Override
    public boolean hasTool(String userId, List<String> toolNames, boolean requireAll) {
        if (toolNames == null || toolNames.isEmpty()) {
            return true;
        }

        List<String> userTools = getUserTools(userId);
        Set<String> userToolSet = new HashSet<>(userTools);

        if (requireAll) {
            // 需要拥有所有工具权限
            return userToolSet.containsAll(toolNames);
        } else {
            // 只需拥有其中一个工具权限
            return toolNames.stream().anyMatch(userToolSet::contains);
        }
    }

    @Override
    public List<String> getRoleNames(String userId) {
        // 查询用户的角色ID列表
        List<UserRole> userRoles = userRoleService.selectByUserId(userId);

        if (userRoles.isEmpty()) {
            return List.of();
        }

        // 根据角色ID查询角色信息
        List<String> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        List<Role> roles = roleService.listByIds(roleIds);

        // 返回角色名称列表
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUserTools(String userId) {
        // 1. 获取用户直接拥有的工具
        List<UserTool> userTools = userToolMapper.selectList(
                new LambdaQueryWrapper<UserTool>().eq(UserTool::getUserId, userId)
        );

        Set<String> toolIds = userTools.stream()
                .map(UserTool::getToolId)
                .collect(Collectors.toSet());

        // 2. 获取用户角色拥有的工具
        List<UserRole> userRoles = userRoleService.selectByUserId(userId);

        if (!userRoles.isEmpty()) {
            List<String> roleIds = userRoles.stream()
                    .map(UserRole::getRoleId)
                    .collect(Collectors.toList());

            List<RoleTool> roleTools = roleToolMapper.selectList(
                    new LambdaQueryWrapper<RoleTool>().in(RoleTool::getRoleId, roleIds)
            );

            // 合并角色工具
            roleTools.stream()
                    .map(RoleTool::getToolId)
                    .forEach(toolIds::add);
        }

        if (toolIds.isEmpty()) {
            return List.of();
        }

        // 3. 查询工具信息
        List<Tool> tools = toolMapper.selectBatchIds(toolIds);

        // 返回工具名称列表
        return tools.stream()
                .map(Tool::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getModelIdsByUserId(String userId) {
        List<UserRole> userRoleList = userRoleService.selectByUserId(userId);
        List<String> userRoles = userRoleList.stream().map(UserRole::getRoleId).toList();

        List<RoleModel> roleModels = roleModelService.selectByRoleIds(userRoles);
        return roleModels.stream()
                .map(RoleModel::getModelId)
                .collect(Collectors.toList());
    }

    @Override
    public Model getModelById(String userId, String modelId) {
        List<String> modelIdsByUserId = getModelIdsByUserId(userId);
        Assert.notNull(modelIdsByUserId, "用户没有模型权限");

        if (!modelIdsByUserId.contains(modelId)) {
            throw new RuntimeException("用户没有模型权限");
        }

        return modelService.getById(modelId);
    }

    @Override
    public ModelRole getModelRoleByUserId(String userId) {
        log.info("[ 用户使用模型权限校验 ] : 用户 {} ", userId);
        User user = userService.getById(userId);
        String modelRole = user.getModelRole();
        return modelRoleService.getByName(modelRole);
    }

    @Override
    public Model getModelByModelName(String userId, String modelName) {
        log.info("[ 用户使用模型权限校验 ] : 用户 {} ，模型 {}", userId, modelName);

        Model model = modelService.getByName(modelName);
        Assert.notNull(model, "模型不存在");

        return getModelById(userId, model.getId());
    }

    @Override
    public boolean canAccessModel(String userId, LlmModelConfiguration config) {
        return true;
    }
}
