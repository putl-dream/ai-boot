package fun.aiboot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.aiboot.entity.RoleTool;
import fun.aiboot.mapper.RoleToolMapper;
import fun.aiboot.service.RoleToolService;
import fun.aiboot.service.ToolService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author putl
 * @since 2025-10-31
 */
@Service
@RequiredArgsConstructor
public class RoleToolServiceImpl extends ServiceImpl<RoleToolMapper, RoleTool> implements RoleToolService {
    private final ToolService toolService;


    @Override
    public List<String> selectToolNameByRoleIds(@NonNull List<String> roleIds) {
        List<String> toolIds = baseMapper.selectList(Wrappers.lambdaQuery(RoleTool.class)
                .in(RoleTool::getRoleId, roleIds)
        ).stream().map(RoleTool::getToolId).toList();

        return toolService.getToolNameByIds(toolIds);
    }

    @Override
    public List<String> selectToolIdsByRoleIds(List<String> roleIds) {
        return baseMapper.selectList(Wrappers.lambdaQuery(RoleTool.class)
                .in(RoleTool::getRoleId, roleIds)
        ).stream().map(RoleTool::getToolId).toList();
    }
}
