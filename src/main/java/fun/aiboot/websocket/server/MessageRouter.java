package fun.aiboot.websocket.server;

public interface MessageRouter {
    void route(String userId, String rawMessage);
}
