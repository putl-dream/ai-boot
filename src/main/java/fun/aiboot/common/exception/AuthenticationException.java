package fun.aiboot.common.exception;

/**
 * 认证异常类
 *
 * @author putl
 * @since 2025-10-30
 */
public class AuthenticationException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(401, message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(401, message, cause);
    }
}
