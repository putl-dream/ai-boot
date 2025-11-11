package fun.aiboot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.aiboot.entity.UserRole;
import fun.aiboot.mapper.UserRoleMapper;
import fun.aiboot.service.RoleService;
import fun.aiboot.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
@RequiredArgsConstructor
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    private final RoleService roleService;


    @Override
    public List<UserRole> selectByUserId(String userId) {
        return baseMapper.selectList(Wrappers.lambdaQuery(UserRole.class)
                .eq(UserRole::getUserId, userId)
        );
    }

    @Override
    public List<String> selectNameByUserId(String userId) {
        List<UserRole> userRoleList = selectByUserId(userId);
        if (userRoleList.isEmpty()) return new ArrayList<>();

        List<String> roleIds = userRoleList.stream().map(UserRole::getRoleId).toList();
        return roleService.getNameByIds(roleIds);
    }

    @Override
    public List<String> selectRoleIdByUserId(String userId) {
        List<UserRole> userRoleList = selectByUserId(userId);
        if (userRoleList.isEmpty()) return new ArrayList<>();

        return userRoleList.stream().map(UserRole::getRoleId).toList();
    }
}
