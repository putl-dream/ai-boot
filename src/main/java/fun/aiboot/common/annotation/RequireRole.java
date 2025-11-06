package fun.aiboot.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限校验注解
 * 用于标注需要特定角色才能访问的方法
 *
 * @author putl
 * @since 2025-10-30
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * 需要的角色ID或角色名称
     */
    String[] value();

    /**
     * 是否需要满足所有角色（AND逻辑）
     * true: 需要拥有所有指定角色
     * false: 只需拥有其中一个角色即可（默认）
     */
    boolean requireAll() default false;
}
