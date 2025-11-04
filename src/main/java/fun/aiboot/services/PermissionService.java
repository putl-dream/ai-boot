package fun.aiboot.services;

import java.util.List;

/**
 * 权限服务接口
 *
 * @author putl
 * @since 2025-10-30
 */
public interface PermissionService {
    /**
     * 用户创建默认角色和工具权限
     */
    void createDefaultRole(String userId);

    /**
     * 检查用户是否拥有指定角色
     *
     * @param userId 用户ID
     * @param roleNames 角色名称列表
     * @param requireAll 是否需要拥有所有角色
     * @return 是否有权限
     */
    boolean hasRole(String userId, List<String> roleNames, boolean requireAll);

    /**
     * 检查用户是否拥有指定工具权限
     *
     * @param userId 用户ID
     * @param toolNames 工具名称列表
     * @param requireAll 是否需要拥有所有工具权限
     * @return 是否有权限
     */
    boolean hasTool(String userId, List<String> toolNames, boolean requireAll);

    /**
     * 获取用户的所有角色名称
     *
     * @param userId 用户ID
     * @return 角色名称列表
     */
    List<String> getUserRoles(String userId);

    /**
     * 获取用户的所有工具名称
     *
     * @param userId 用户ID
     * @return 工具名称列表
     */
    List<String> getUserTools(String userId);

    /**
     * 获取用户所有角色的模型ID列表
     *
     * @param userId 用户ID
     * @return 模型ID列表
     */
    List<String> getUserModels(String userId);
}
