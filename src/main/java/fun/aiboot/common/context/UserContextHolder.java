package fun.aiboot.common.context;

import java.time.LocalDateTime;
import java.util.List;

public class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    public static void set(UserContext context) {
        CONTEXT.set(context);
    }

    public static UserContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static String getUserId() {
        UserContext context = get();
        return context == null ? null : context.getUserId();
    }

    public static String getUsername() {
        UserContext context = get();
        return context == null ? null : context.getUsername();
    }

    public static List<String> getRoles() {
        UserContext context = get();
        return context == null ? null : context.getRoles();
    }

    public static List<String> getRoleModelIds() {
        UserContext context = get();
        return context == null ? null : context.getRoleModelIds();
    }

    public static List<String> getRoleToolIds() {
        UserContext context = get();
        return context == null ? null : context.getRoleToolIds();
    }

    public static String getCurrentModelId() {
        UserContext context = get();
        return context == null ? null : context.getCurrentModelId();
    }

    public static LocalDateTime getLastUpdated() {
        UserContext context = get();
        return context == null ? null : context.getLastUpdated();
    }
}


