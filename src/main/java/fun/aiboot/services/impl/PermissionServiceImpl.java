package fun.aiboot.services.impl;

import fun.aiboot.service.RoleModelService;
import fun.aiboot.service.RoleToolService;
import fun.aiboot.service.UserRoleService;
import fun.aiboot.services.PermissionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final RoleModelService roleModelService;
    private final RoleToolService roleToolService;

    @Override
    public boolean hasRoleName(@NonNull String userId, @NonNull List<String> roleNames, boolean requireAll) {
        if (roleNames.isEmpty()) return false;

        List<String> userRoles = userRoleService.selectNameByUserId(userId);
        Set<String> userRoleSet = new HashSet<>(userRoles);

        if (requireAll) {
            return userRoleSet.containsAll(roleNames); // 需要拥有所有角色
        } else {
            return roleNames.stream().anyMatch(userRoleSet::contains); // 只需拥有其中一个角色
        }
    }

    @Override
    public boolean hasToolIds(@NonNull String userId, @NonNull List<String> toolIds, boolean requireAll) {
        if (toolIds.isEmpty()) return false;

        List<String> roleIds = userRoleService.selectRoleIdByUserId(userId);
        List<String> userTools = roleToolService.selectToolIdsByRoleIds(roleIds);
        Set<String> userToolSet = new HashSet<>(userTools);

        if (requireAll) {
            return userToolSet.containsAll(toolIds); // 需要拥有所有工具权限
        } else {
            return toolIds.stream().anyMatch(userToolSet::contains); // 只需拥有其中一个工具权限
        }
    }

    @Override
    public boolean hasToolNames(String userId, List<String> toolNames, boolean requireAll) {
        if (toolNames.isEmpty()) return false;

        List<String> roleIds = userRoleService.selectRoleIdByUserId(userId);
        List<String> toolIds = roleToolService.selectToolNameByRoleIds(roleIds);
        Set<String> toolIdSet = new HashSet<>(toolIds);

        if (requireAll) {
            return toolIdSet.containsAll(toolNames); // 需要拥有所有工具权限
        } else {
            return toolNames.stream().anyMatch(toolIdSet::contains); // 只需拥有其中一个工具权限
        }
    }

    @Override
    public boolean hasModelNames(String userId, List<String> modelNames, boolean requireAll) {
        if (modelNames.isEmpty()) return false;

        List<String> roleIds = userRoleService.selectRoleIdByUserId(userId);
        List<String> toolNames = roleModelService.selectModelNameByRoleIds(roleIds);
        Set<String> userToolSet = new HashSet<>(toolNames);

        if (requireAll) {
            return userToolSet.containsAll(modelNames); // 需要拥有所有模型权限
        } else {
            return modelNames.stream().anyMatch(userToolSet::contains); // 只需拥有其中一个模型权限
        }
    }

    @Override
    public boolean hasModelIds(@NonNull String userId, @NonNull List<String> modelIds, boolean requireAll) {
        if (modelIds.isEmpty()) return false;

        List<String> roleIds = userRoleService.selectRoleIdByUserId(userId);
        List<String> modelNameList = roleModelService.selectModelIdByIds(roleIds);
        Set<String> modelNameSet = new HashSet<>(modelNameList);

        if (requireAll) {
            return modelNameSet.containsAll(modelIds); // 需要拥有所有模型权限
        } else {
            return modelIds.stream().anyMatch(modelNameSet::contains); // 只需拥有其中一个模型权限
        }
    }
}
