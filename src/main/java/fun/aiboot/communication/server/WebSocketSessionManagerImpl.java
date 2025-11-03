package fun.aiboot.communication.server;

import fun.aiboot.dialogue.llm.tool.ToolsGlobalRegistry;
import fun.aiboot.exception.AuthorizationException;
import fun.aiboot.service.ConversationService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WebSocketSessionManagerImpl implements SessionManager {

    @Resource
    ToolsGlobalRegistry toolsGlobalRegistry;
    @Resource
    ConversationService conversationService;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();


    @Override
    public void register(String userId, WebSocketSession session) {
        /*
         * 原理 ：
         * 用户在前端通过 http 请求创建会话，返回会话ID
         * 前端通过 websocket 连接，传入会话ID，将会话id保存在 session 中
         * 消息发送时，会话ID会保存在 session 中，通过 sessionId 获取会话ID记录到Message表中
         */
        String conversationId = String.valueOf(session.getAttributes().get(WebSocketConstants.Conversation_Id));
        if (StringUtils.isNotEmpty(conversationId)) {
            throw new AuthorizationException("会话id未找到");
        }
        sessions.put(userId, session);
        log.info("用户 {} 会话创建成功：{}", userId, conversationId);
    }

    @Override
    public void remove(String userId) {
        sessions.remove(userId);
    }

    @Override
    public WebSocketSession getSession(String userId) {
        return sessions.get(userId);
    }

    @Override
    public String getSessionId(String userId) {
        return sessions.get(userId).getId();
    }

    @Override
    public Collection<WebSocketSession> getAllSessions() {
        return sessions.values();
    }
}
