package fun.aiboot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.aiboot.entity.User;
import fun.aiboot.mapper.UserMapper;
import fun.aiboot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @auther putl
 * @since 2025-10-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getByUserName(String username) {
        return getOne(Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username)
        );
    }

    @Override
    public long countName(String username) {
        return count(Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username)
        );
    }
}
