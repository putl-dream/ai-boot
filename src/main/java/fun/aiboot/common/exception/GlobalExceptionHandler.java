package fun.aiboot.common.exception;

import fun.aiboot.common.Result;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author putl
 * @since 2025-10-30
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常：{}", e.getMessage(), e);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.error("认证异常：{}", e.getMessage(), e);
        return Result.error(401, e.getMessage());
    }

    /**
     * 处理授权异常
     */
    @ExceptionHandler(AuthorizationException.class)
    public Result<Void> handleAuthorizationException(AuthorizationException e) {
        log.error("授权异常：{}", e.getMessage(), e);
        return Result.error(403, e.getMessage());
    }

    /**
     * 处理JWT过期异常
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public Result<Void> handleExpiredJwtException(ExpiredJwtException e) {
        log.error("JWT已过期：{}", e.getMessage());
        return Result.error(401, "登录已过期，请重新登录");
    }

    /**
     * 处理JWT格式错误异常
     */
    @ExceptionHandler(MalformedJwtException.class)
    public Result<Void> handleMalformedJwtException(MalformedJwtException e) {
        log.error("JWT格式错误：{}", e.getMessage());
        return Result.error(401, "无效的token");
    }

    /**
     * 处理参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验失败：{}", errors);
        return Result.error(400, "参数校验失败：" + errors);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数绑定失败：{}", errors);
        return Result.error(400, "参数错误：" + errors);
    }

    /**
     * 处理SQL约束违反异常（如唯一键冲突）
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<Void> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        log.error("数据库约束异常：{}", e.getMessage());
        String message = e.getMessage();
        if (message.contains("Duplicate entry")) {
            return Result.error(400, "数据已存在，请勿重复添加");
        }
        return Result.error(500, "数据库操作失败");
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<Void> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常：", e);
        return Result.error(500, "系统内部错误");
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("非法参数异常：{}", e.getMessage(), e);
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error(500, "系统错误，请联系管理员");
    }
}
