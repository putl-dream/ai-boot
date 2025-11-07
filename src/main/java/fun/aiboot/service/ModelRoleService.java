package fun.aiboot.service;

import fun.aiboot.entity.ModelRole;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 系统提示词 服务类
 * </p>
 *
 * @author putl
 * @since 2025-11-05
 */
public interface ModelRoleService extends IService<ModelRole> {
    /**
     * 根据名称查询
     * @param name 名称
     * @return 系统提示词
     */
    ModelRole getByName(String name);
}
