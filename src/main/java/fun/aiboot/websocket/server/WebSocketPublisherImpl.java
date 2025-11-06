package fun.aiboot.websocket.server;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Service
public class WebSocketPublisherImpl implements MessagePublisher {

    private final SessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketPublisherImpl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void sendToUser(String userId, Object message) {
        WebSocketSession session = sessionManager.getSession(userId);
        if (session != null && session.isOpen()) {
            try {
                String payload = JSON.toJSONString(message);
                session.sendMessage(new TextMessage(payload));
            } catch (Exception e) {
                log.error("发送消息时发生错误: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void broadcast(Object message) {
        sessionManager.getAllSessions().forEach(s -> {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                } catch (Exception ignored) {
                }
            }
        });
    }
}
