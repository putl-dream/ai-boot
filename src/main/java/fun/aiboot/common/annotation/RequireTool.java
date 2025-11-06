package fun.aiboot.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 工具权限校验注解
 * 用于标注需要特定工具权限才能访问的方法
 *
 * @author putl
 * @since 2025-10-30
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireTool {

    /**
     * 需要的工具ID或工具名称
     */
    String[] value();

    /**
     * 是否需要满足所有工具权限（AND逻辑）
     * true: 需要拥有所有指定工具权限
     * false: 只需拥有其中一个工具权限即可（默认）
     */
    boolean requireAll() default false;
}
