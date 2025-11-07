package fun.aiboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.aiboot.entity.User;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @auther putl
 * @since 2025-10-30
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return  用户
     */
    User getByUserName(String username);

    /**
     * 根据用户名查询用户数量
     * @param username 用户名
     * @return 用户数量
     */
    long countName(String username);
}
