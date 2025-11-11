package fun.aiboot.service;

import fun.aiboot.entity.RoleTool;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.NonNull;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author putl
 * @since 2025-10-31
 */
public interface RoleToolService extends IService<RoleTool> {

    List<String> selectToolNameByRoleIds(@NonNull List<String> roleIds);

    List<String> selectToolIdsByRoleIds(List<String> roleIds);

}
