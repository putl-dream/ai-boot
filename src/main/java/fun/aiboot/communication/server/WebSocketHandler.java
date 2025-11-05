package fun.aiboot.communication.server;

import fun.aiboot.context.UserContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

/**
 * 从技术与业务来看：
 * WebSocketHandler 相当于一个event listener，主要负责技术层事件，比如：连接建立成功、接收到消息、连接关闭、连接出错。
 * 因此这里bean只负责技术层事件，不处理业务逻辑。
 */

@Slf4j
@Component
public class WebSocketHandler extends AbstractWebSocketHandler {

    @Resource
    private SessionManager sessionManager;

    @Resource
    private MessageRouter messageRouter;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UserContext userContext = (UserContext) session.getAttributes().get("userContext");
        log.info("【{}】用户发起【{}】会话请求", userContext.getUsername(), session.getAttributes().get(WebSocketConstants.Conversation_Id));
        sessionManager.register(userContext.getUserId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("接收到文本消息：\n{}", message.getPayload());
        UserContext userContext = (UserContext) session.getAttributes().get("userContext");
        messageRouter.route(userContext.getUserId(), message.getPayload());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        log.info("接收到二进制消息：{}", message.getPayload());
        UserContext userContext = (UserContext) session.getAttributes().get("userContext");
        messageRouter.route(userContext.getUserId(), message.getPayload().toString());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.warn("连接关闭：{}", status.getReason());
        UserContext userContext = (UserContext) session.getAttributes().get("userContext");
        sessionManager.remove(userContext.getUserId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("连接出错：{}", exception.getMessage(), exception);
        UserContext userContext = (UserContext) session.getAttributes().get("userContext");
        sessionManager.remove(userContext.getUserId());
    }
}
