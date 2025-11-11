package fun.aiboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.aiboot.entity.Role;

import java.util.List;

/**
 * <p>
 * 角色 服务类
 * </p>
 *
 * @author putl
 * @since 2025-10-30
 */
public interface RoleService extends IService<Role> {

    /**
     * 根据名称获取角色
     *
     * @param name 角色名称
     * @return 角色
     */
    Role getByName(String name);

    /**
     * 根据ID列表获取角色
     *
     * @param ids ID列表
     * @return 角色列表
     */
    List<Role> getByIds(List<String> ids);


    /**
     * 根据ID列表获取角色名称
     *
     * @param ids ID列表
     * @return 角色名称列表
     */
    List<String> getNameByIds(List<String> ids);

}
