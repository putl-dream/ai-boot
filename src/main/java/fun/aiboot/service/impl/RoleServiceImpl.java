package fun.aiboot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.aiboot.entity.Role;
import fun.aiboot.mapper.RoleMapper;
import fun.aiboot.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 角色 服务实现类
 * </p>
 *
 * @author putl
 * @since 2025-10-30
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public Role getByName(String name) {
        return getOne(Wrappers.lambdaQuery(Role.class).eq(Role::getName, name));
    }

    @Override
    public List<Role> getByIds(List<String> ids) {
        return listByIds(ids);
    }

    @Override
    public List<String> getNameByIds(List<String> ids) {
        return baseMapper.selectList(Wrappers.lambdaQuery(Role.class)
                        .select(Role::getName)
                        .in(Role::getId, ids)
                ).stream().map(Role::getName)
                .toList();
    }
}
