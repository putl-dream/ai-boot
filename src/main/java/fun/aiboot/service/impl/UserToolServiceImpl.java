package fun.aiboot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.aiboot.entity.UserTool;
import fun.aiboot.mapper.UserToolMapper;
import fun.aiboot.service.ToolService;
import fun.aiboot.service.UserRoleService;
import fun.aiboot.service.UserToolService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户工具关联服务实现类
 * </p>
 *
 * @author putl
 * @since 2025-10-30
 */
@Service
@RequiredArgsConstructor
public class UserToolServiceImpl extends ServiceImpl<UserToolMapper, UserTool> implements UserToolService {
    private final ToolService toolService;
    private final UserRoleService userRoleService;
}
