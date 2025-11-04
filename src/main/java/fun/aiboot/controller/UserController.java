package fun.aiboot.controller;

import fun.aiboot.annotation.RequireRole;
import fun.aiboot.annotation.RequireTool;
import fun.aiboot.common.Result;
import fun.aiboot.context.UserContextHolder;
import fun.aiboot.entity.User;
import fun.aiboot.services.CertificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author putl
 * @since 2025-10-30
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final CertificationService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());
        return Result.success("登录成功", token);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {
        userService.register(request.getUsername(), request.getPassword(), request.getEmail());
        return Result.success("注册成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        String userId = UserContextHolder.getUserId();
        User user = userService.getUser(userId);
        // 清除密码字段
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 修改密码
     */
    @PostMapping("/password")
    public Result<String> updatePassword(@RequestBody UpdatePasswordRequest request) {
        String username = UserContextHolder.getUsername();
        userService.updatePassword(username, request.getOldPassword(), request.getNewPassword());
        return Result.success("密码修改成功");
    }

    /**
     * 忘记密码
     */
    @PostMapping("/forget-password")
    public Result<String> forgetPassword(@RequestBody ForgetPasswordRequest request) {
        userService.forgetPassword(request.getUsername(), request.getEmail());
        return Result.success("密码重置成功，请查收邮件");
    }

    /**
     * 需要管理员角色才能访问的接口示例
     */
    @GetMapping("/admin")
    @RequireRole("admin")
    public Result<String> adminOnly() {
        return Result.success("欢迎管理员：" + UserContextHolder.getUsername());
    }

    /**
     * 需要特定工具权限才能访问的接口示例
     */
    @GetMapping("/tool-test")
    @RequireTool("image-generator")
    public Result<String> toolTest() {
        return Result.success("您拥有图片生成工具权限");
    }

    /**
     * 需要多个角色或工具权限的接口示例
     */
    @GetMapping("/advanced")
    @RequireRole(value = {"admin", "vip"}, requireAll = false)
    @RequireTool(value = {"advanced-search", "data-export"}, requireAll = true)
    public Result<String> advancedFeature() {
        return Result.success("您拥有高级功能权限");
    }

    // === DTO类定义 ===

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    static class RegisterRequest {
        private String username;
        private String password;
        private String email;
    }

    @Data
    static class UpdatePasswordRequest {
        private String oldPassword;
        private String newPassword;
    }

    @Data
    static class ForgetPasswordRequest {
        private String username;
        private String email;
    }
}
