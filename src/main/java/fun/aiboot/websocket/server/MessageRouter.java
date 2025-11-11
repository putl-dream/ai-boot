package fun.aiboot.websocket.server;

import fun.aiboot.common.context.UserContext;

public interface MessageRouter {
    void route(UserContext userContext, String rawMessage);
}
