package fun.aiboot.interceptor;

import fun.aiboot.context.UserContext;
import fun.aiboot.context.UserContextHolder;
import fun.aiboot.exception.AuthenticationException;
import fun.aiboot.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 *
 * @author putl
 * @since 2025-10-30
 */
@Slf4j
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    /**
     * 在请求处理之前进行调用
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头中获取token
            String token = request.getHeader("Authorization");

        // 如果token为空，抛出认证异常
        if (!StringUtils.hasText(token)) {
            throw new AuthenticationException("未登录，请先登录");
        }

        // 去除Bearer前缀（如果有）
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // 解析token
            UserContext userContext = JwtUtil.parseJWT(token);

            log.debug("用户认证成功：userId={}, username={}", userContext.getUserId(), userContext.getUsername());

            // 将用户信息存储到ThreadLocal中
            UserContextHolder.set(userContext);

            log.debug("用户赋权成功：角色={}，支持模型{}", userContext.getRoles(), userContext.getRoleModelIds());
            return true;
        } catch (Exception e) {
            log.error("token解析失败：{}", e.getMessage());
            throw new AuthenticationException("token无效或已过期");
        }
    }

    /**
     * 请求处理完成后进行调用
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除用户上下文，防止内存泄漏
        UserContextHolder.clear();
    }
}
