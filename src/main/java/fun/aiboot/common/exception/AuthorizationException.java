package fun.aiboot.common.exception;

/**
 * 授权异常类（权限不足）
 *
 * @author putl
 * @since 2025-10-30
 */
public class AuthorizationException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public AuthorizationException(String message) {
        super(403, message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(403, message, cause);
    }
}
