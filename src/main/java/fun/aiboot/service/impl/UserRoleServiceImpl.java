package fun.aiboot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.aiboot.entity.UserRole;
import fun.aiboot.mapper.UserRoleMapper;
import fun.aiboot.service.UserRoleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户角色关联服务实现类
 * </p>
 *
 * @author putl
 * @since 2025-10-30
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Override
    public List<UserRole> selectByUserId(String userId) {
        return baseMapper.selectList(Wrappers.lambdaQuery(UserRole.class)
                .eq(UserRole::getUserId, userId)
        );
    }
}
