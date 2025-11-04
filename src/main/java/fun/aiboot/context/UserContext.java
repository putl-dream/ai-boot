package fun.aiboot.context;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户上下文工具类
 * 用于在ThreadLocal中存储当前登录用户信息
 *
 * @author putl
 * @since 2025-10-30
 */
@Data
@Builder
public class UserContext {
    // 用户信息
    private String userId;
    private String username;

    // 角色权限（静态部分）
    private List<String> roles;
    private List<String> roleModelIds;
    private List<String> roleToolIds;

    // 用户动态上下文（运行态部分）
    private String currentModelId;

    // 缓存时间戳（用于刷新）
    private LocalDateTime lastUpdated;
}
