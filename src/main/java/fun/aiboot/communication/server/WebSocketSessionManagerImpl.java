package fun.aiboot.communication.server;

import fun.aiboot.context.UserContext;
import fun.aiboot.dialogue.llm.tool.ToolsGlobalRegistry;
import fun.aiboot.entity.Conversation;
import fun.aiboot.service.ConversationService;
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
        sessions.put(userId, session);
        // 创建会话持久化到数据库
        UserContext userContext = (UserContext) session.getAttributes().get("userContext");
        conversationService.save(Conversation.builder()
                .id(getSessionId(userId))
                .userId(userId)
                .title("New Conversation")
                .modelId(userContext.getCurrentModelId())
                .build());
        log.info("用户 {} 会话创建成功：{}", userId, getSessionId(userId));
//        toolsGlobalRegistry.getAllFunctions();
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
