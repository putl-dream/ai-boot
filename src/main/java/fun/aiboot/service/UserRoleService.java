package fun.aiboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.aiboot.entity.UserRole;

import java.util.List;

/**
 * <p>
 * 用户角色关联服务接口
 * </p>
 *
 * @author putl
 * @since 2025-10-30
 */
public interface UserRoleService extends IService<UserRole> {

    /**
     * 根据用户ID查询用户角色关联
     *
     * @param userId 用户ID
     * @return 用户角色关联列表
     */
    List<UserRole> selectByUserId(String userId);

    /**
     * 根据角色ID查询用户角色名称
     *
     * @param userId 用户ID
     * @return 用户角色名称
     */
    List<String> selectNameByUserId(String userId);

    /**
     * 根据用户ID查询用户角色ID
     *
     * @param userId 用户ID
     * @return 用户角色ID
     */
    List<String> selectRoleIdByUserId(String userId);
}
