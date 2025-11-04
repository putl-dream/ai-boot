package fun.aiboot.aspect;

import fun.aiboot.annotation.RequireRole;
import fun.aiboot.annotation.RequireTool;
import fun.aiboot.context.UserContextHolder;
import fun.aiboot.exception.AuthorizationException;
import fun.aiboot.services.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 权限校验切面
 *
 * @author putl
 * @since 2025-10-30
 */
@Slf4j
@Aspect
@Component
@Order(1) // 确保在认证之后执行
@RequiredArgsConstructor
public class PermissionAspect {

    private final PermissionService permissionService;

    /**
     * 角色权限校验
     */
    @Before("@annotation(fun.aiboot.annotation.RequireRole) || @within(fun.aiboot.annotation.RequireRole)")
    public void checkRole(JoinPoint joinPoint) {
        // 获取当前用户ID
        String userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new AuthorizationException("用户未登录");
        }

        // 获取注解信息
        RequireRole requireRole = getAnnotation(joinPoint, RequireRole.class);
        if (requireRole == null) {
            return;
        }

        String[] roleNames = requireRole.value();
        boolean requireAll = requireRole.requireAll();

        log.debug("检查用户 {} 的角色权限：{}, requireAll={}", userId, Arrays.toString(roleNames), requireAll);

        // 检查权限
        boolean hasPermission = permissionService.hasRole(userId, Arrays.asList(roleNames), requireAll);

        if (!hasPermission) {
            String message = requireAll
                    ? String.format("权限不足，需要拥有以下所有角色：%s", Arrays.toString(roleNames))
                    : String.format("权限不足，需要拥有以下任一角色：%s", Arrays.toString(roleNames));
            log.warn("用户 {} 权限校验失败：{}", userId, message);
            throw new AuthorizationException(message);
        }

        log.debug("用户 {} 角色权限校验通过", userId);
    }

    /**
     * 工具权限校验
     */
    @Before("@annotation(fun.aiboot.annotation.RequireTool) || @within(fun.aiboot.annotation.RequireTool)")
    public void checkTool(JoinPoint joinPoint) {
        // 获取当前用户ID
        String userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new AuthorizationException("用户未登录");
        }

        // 获取注解信息
        RequireTool requireTool = getAnnotation(joinPoint, RequireTool.class);
        if (requireTool == null) {
            return;
        }

        String[] toolNames = requireTool.value();
        boolean requireAll = requireTool.requireAll();

        log.debug("检查用户 {} 的工具权限：{}, requireAll={}", userId, Arrays.toString(toolNames), requireAll);

        // 检查权限
        boolean hasPermission = permissionService.hasTool(userId, Arrays.asList(toolNames), requireAll);

        if (!hasPermission) {
            String message = requireAll
                    ? String.format("权限不足，需要拥有以下所有工具权限：%s", Arrays.toString(toolNames))
                    : String.format("权限不足，需要拥有以下任一工具权限：%s", Arrays.toString(toolNames));
            log.warn("用户 {} 工具权限校验失败：{}", userId, message);
            throw new AuthorizationException(message);
        }

        log.debug("用户 {} 工具权限校验通过", userId);
    }

    /**
     * 获取方法或类上的注解
     */
    private <T extends java.lang.annotation.Annotation> T getAnnotation(JoinPoint joinPoint, Class<T> annotationClass) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 先检查方法上的注解
        T annotation = method.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }

        // 再检查类上的注解
        return joinPoint.getTarget().getClass().getAnnotation(annotationClass);
    }
}
