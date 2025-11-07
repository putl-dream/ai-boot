package fun.aiboot.service;

import fun.aiboot.entity.UserRole;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
