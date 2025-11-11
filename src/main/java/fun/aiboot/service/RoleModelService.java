package fun.aiboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.aiboot.entity.RoleModel;

import java.util.List;

/**
 * <p>
 * 角色与模型关联表 服务类
 * </p>
 *
 * @author putl
 * @since 2025-10-30
 */
public interface RoleModelService extends IService<RoleModel> {

    /**
     * 根据角色ID列表获取模型
     *
     * @param roleIds 角色ID列表
     * @return 模型列表
     */
    List<RoleModel> selectByRoleIds(List<String> roleIds);

    /**
     * 根据角色ID列表获取模型ID
     *
     * @param roleIds 角色ID列表
     * @return 模型ID列表
     */
    List<String> selectModelIdByIds(List<String> roleIds);


    /**
     * 根据角色ID列表获取模型名称
     *
     * @param roleIds 角色ID列表
     * @return 模型名称列表
     */
    List<String> selectModelNameByRoleIds(List<String> roleIds);
}
