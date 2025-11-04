package fun.aiboot.services;

public interface AuthService {
    String login(String username, String password);

    void register(String username, String password, String email);

    void updatePassword(String username, String oldPassword, String newPassword);

    void forgetPassword(String username, String email);
}
