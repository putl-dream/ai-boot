package fun.aiboot.service;

import fun.aiboot.entity.Tool;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 工具 服务类
 * </p>
 *
 * @author putl
 * @since 2025-10-31
 */
public interface ToolService extends IService<Tool> {

    /**
     * 判断工具是否存在
     * @param name 工具名称
     * @return true:不存在 false:存在
     */
    boolean isEmpty(String name);


    List<Tool> getByIds(List<String> toolIds);


    List<String> getToolNameByIds(List<String> toolIds);
}
