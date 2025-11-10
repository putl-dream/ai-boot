package fun.aiboot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.aiboot.entity.Tool;
import fun.aiboot.mapper.ToolMapper;
import fun.aiboot.service.ToolService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 工具 服务实现类
 * </p>
 *
 * @author putl
 * @since 2025-10-31
 */
@Service
public class ToolServiceImpl extends ServiceImpl<ToolMapper, Tool> implements ToolService {

    @Override
    public boolean isEmpty(String name) {
        return count(Wrappers.lambdaQuery(Tool.class).eq(Tool::getName, name)) == 0;
    }
}
