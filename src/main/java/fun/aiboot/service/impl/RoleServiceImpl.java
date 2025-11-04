package fun.aiboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.aiboot.entity.Role;
import fun.aiboot.mapper.RoleMapper;
import fun.aiboot.service.RoleService;
import org.springframework.stereotype.Service;

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

}
