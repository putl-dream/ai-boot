package fun.aiboot.service;

import fun.aiboot.entity.RoleModel;
import com.baomidou.mybatisplus.extension.service.IService;

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

    List<RoleModel> selectByRoleIds(List<String> roleIds);
}
