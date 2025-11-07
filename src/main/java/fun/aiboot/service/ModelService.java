package fun.aiboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.aiboot.entity.Model;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author putl
 * @since 2025-10-31
 */
public interface ModelService extends IService<Model> {
    /**
     * 根据名称查询模型
     *
     * @param name 模型名称
     * @return 模型
     */
    Model getByName(String name);
}
