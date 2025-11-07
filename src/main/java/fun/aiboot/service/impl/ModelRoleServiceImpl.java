package fun.aiboot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.aiboot.entity.ModelRole;
import fun.aiboot.mapper.ModelRoleMapper;
import fun.aiboot.service.ModelRoleService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统提示词 服务实现类
 * </p>
 *
 * @author putl
 * @since 2025-11-05
 */
@Service
public class ModelRoleServiceImpl extends ServiceImpl<ModelRoleMapper, ModelRole> implements ModelRoleService {

    @Override
    public ModelRole getByName(String name) {
        return this.getOne(Wrappers.lambdaQuery(ModelRole.class).eq(ModelRole::getName, name));
    }
}
