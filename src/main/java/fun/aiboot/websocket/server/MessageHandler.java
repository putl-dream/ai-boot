package fun.aiboot.websocket.server;


import fun.aiboot.common.context.UserContext;
import fun.aiboot.websocket.domain.BaseMessage;

public interface MessageHandler {

    /**
     * 返回此处理器支持的消息类型
     * 例如 "chat", "notify", "order_update"
     */
    String getType();

    /**
     * 处理消息
     */
    void handleMessage(UserContext userContext, BaseMessage message);
}
