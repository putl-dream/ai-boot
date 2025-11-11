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
     * 检查用户是否拥有指定角色
     *
     * @param userId     用户ID
     * @param roleNames  角色名称列表
     * @param requireAll 是否需要拥有所有角色
     * @return 是否有权限
     */
    boolean hasRoleName(String userId, List<String> roleNames, boolean requireAll);

    /**
     * 检查用户是否拥有指定工具权限
     *
     * @param userId     用户ID
     * @param toolIds    工具Id列表
     * @param requireAll 是否需要拥有所有工具权限
     * @return 是否有权限
     */
    boolean hasToolIds(String userId, List<String> toolIds, boolean requireAll);

    /**
     * 检查用户是否拥有指定工具权限
     *
     * @param userId 用户ID
     *               模型名称列表
     *               是否需要所有模型可访问
     * @return 是否有权限
     */
    boolean hasModelNames(String userId, List<String> modelNames, boolean requireAll);

    /**
     * 检查模型是否可访问
     *
     * @param userId     用户ID
     * @param modelIds   模型Id列表
     * @param requireAll 是否需要所有模型可访问
     * @return 是否可访问
     */
    boolean hasModelIds(String userId, List<String> modelIds, boolean requireAll);

    /**
     * 检查用户是否拥有指定工具权限
     *
     * @param userId     用户ID
     * @param toolNames  工具名称列表
     * @param requireAll 是否需要拥有所有工具权限
     * @return 是否有权限
     */
    boolean hasToolNames(String userId, List<String> toolNames, boolean requireAll);
}
