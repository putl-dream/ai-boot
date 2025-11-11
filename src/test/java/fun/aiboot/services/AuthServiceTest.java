package fun.aiboot.services;

import fun.aiboot.entity.User;
import fun.aiboot.common.exception.BusinessException;
import fun.aiboot.service.UserService;
import fun.aiboot.services.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_Success() {
        // Given
        String username = "testuser";
        String password = "testpass";
        String encodedPassword = "encodedPass";
        User user = User.builder()
                .id("1")
                .username(username)
                .password(encodedPassword)
                .build();

        when(userService.getOne(any())).thenReturn(user);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        // When
        String token = authService.login(username, password);

        // Then
        assertNotNull(token);
        verify(userService).getOne(any());
        verify(passwordEncoder).matches(password, encodedPassword);
    }

    @Test
    void login_UserNotFound_ThrowsBusinessException() {
        // Given
        String username = "nonexistent";
        String password = "testpass";

        when(userService.getOne(any())).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(username, password);
        });

        assertEquals(401, exception.getCode());
        assertEquals("用户名或密码错误", exception.getMessage());
        verify(userService).getOne(any());
    }

    @Test
    void login_InvalidPassword_ThrowsBusinessException() {
        // Given
        String username = "testuser";
        String password = "wrongpass";
        String encodedPassword = "encodedPass";
        User user = User.builder()
                .id("1")
                .username(username)
                .password(encodedPassword)
                .build();

        when(userService.getOne(any())).thenReturn(user);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(username, password);
        });

        assertEquals(401, exception.getCode());
        assertEquals("用户名或密码错误", exception.getMessage());
        verify(userService).getOne(any());
        verify(passwordEncoder).matches(password, encodedPassword);
    }

    @Test
    void register_Success() {
        // Given
        String username = "newuser";
        String password = "newpass";
        String email = "newuser@example.com";
        String encodedPassword = "encodedNewPass";

        when(userService.count(any())).thenReturn(0L);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        // When
        assertDoesNotThrow(() -> {
//            authService.register(username, password, email);
        });

        // Then
        verify(userService).count(any());
        verify(passwordEncoder).encode(password);
        verify(userService).save(any(User.class));
    }

    @Test
    void register_UsernameExists_ThrowsBusinessException() {
        // Given
        String username = "existinguser";
        String password = "newpass";
        String email = "newuser@example.com";

        when(userService.count(any())).thenReturn(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
//            authService.register(username, password, email);
        });

        assertEquals("用户名已存在", exception.getMessage());
        verify(userService).count(any());
    }

    @Test
    void updatePassword_Success() {
        // Given
        String username = "testuser";
        String oldPassword = "oldpass";
        String newPassword = "newpass";
        String encodedOldPassword = "encodedOldPass";
        String encodedNewPassword = "encodedNewPass";
        User user = User.builder()
                .id("1")
                .username(username)
                .password(encodedOldPassword)
                .build();

        when(userService.getOne(any())).thenReturn(user);
        when(passwordEncoder.matches(oldPassword, encodedOldPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        // When
        assertDoesNotThrow(() -> {
            authService.updatePassword(username, oldPassword, newPassword);
        });

        // Then
        verify(userService).getOne(any());
        verify(passwordEncoder).matches(oldPassword, encodedOldPassword);
        verify(passwordEncoder).encode(newPassword);
        verify(userService).updateById(any(User.class));
    }

    @Test
    void updatePassword_UserNotFound_ThrowsBusinessException() {
        // Given
        String username = "nonexistent";
        String oldPassword = "oldpass";
        String newPassword = "newpass";

        when(userService.getOne(any())).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.updatePassword(username, oldPassword, newPassword);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(userService).getOne(any());
    }

    @Test
    void updatePassword_InvalidOldPassword_ThrowsBusinessException() {
        // Given
        String username = "testuser";
        String oldPassword = "wrongpass";
        String newPassword = "newpass";
        String encodedOldPassword = "encodedOldPass";
        User user = User.builder()
                .id("1")
                .username(username)
                .password(encodedOldPassword)
                .build();

        when(userService.getOne(any())).thenReturn(user);
        when(passwordEncoder.matches(oldPassword, encodedOldPassword)).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.updatePassword(username, oldPassword, newPassword);
        });

        assertEquals("旧密码错误", exception.getMessage());
        verify(userService).getOne(any());
        verify(passwordEncoder).matches(oldPassword, encodedOldPassword);
    }

    @Test
    void forgetPassword_Success() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String encodedPassword = "encodedTempPass";
        User user = User.builder()
                .id("1")
                .username(username)
                .email(email)
                .password("oldpass")
                .build();

        when(userService.getOne(any())).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);

        // When
        assertDoesNotThrow(() -> {
            authService.forgetPassword(username, email);
        });

        // Then
        verify(userService).getOne(any());
        verify(passwordEncoder).encode(anyString());
        verify(userService).updateById(any(User.class));
    }

    @Test
    void forgetPassword_UserNotFound_ThrowsBusinessException() {
        // Given
        String username = "nonexistent";
        String email = "test@example.com";

        when(userService.getOne(any())).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.forgetPassword(username, email);
        });

        assertEquals("用户名或邮箱错误", exception.getMessage());
        verify(userService).getOne(any());
    }
}