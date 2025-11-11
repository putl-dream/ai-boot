package fun.aiboot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.aiboot.entity.RoleModel;
import fun.aiboot.mapper.RoleModelMapper;
import fun.aiboot.service.ModelService;
import fun.aiboot.service.RoleModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 角色与模型关联表 服务实现类
 * </p>
 *
 * @author putl
 * @since 2025-10-30
 */
@Service
@RequiredArgsConstructor
public class RoleModelServiceImpl extends ServiceImpl<RoleModelMapper, RoleModel> implements RoleModelService {

    private final ModelService modelService;

    @Override
    public List<RoleModel> selectByRoleIds(List<String> roleIds) {
        return baseMapper.selectList(Wrappers.lambdaQuery(RoleModel.class)
                .in(RoleModel::getRoleId, roleIds)
        );
    }

    @Override
    public List<String> selectModelIdByIds(List<String> roleIds) {
        return baseMapper.selectList(Wrappers.lambdaQuery(RoleModel.class)
                .select(RoleModel::getModelId)
                .in(RoleModel::getRoleId, roleIds)
        ).stream().map(RoleModel::getModelId).toList();
    }

    @Override
    public List<String> selectModelNameByRoleIds(List<String> roleIds) {
        List<String> modelIds = selectModelIdByIds(roleIds);
        return modelService.getNameByIds(modelIds);
    }
}
