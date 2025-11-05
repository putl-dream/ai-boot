package fun.aiboot.dialogue.llm.context;

import fun.aiboot.communication.server.SessionManager;
import fun.aiboot.communication.server.WebSocketConstants;
import fun.aiboot.mapper.MessageMapper;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySQLDialogueContext implements DialogueContext {


    private final Map<String, Message> memory = new HashMap<>();

    private final MessageMapper messageMapper;
    private final SessionManager sessionManager;

    public MySQLDialogueContext(MessageMapper messageMapper, SessionManager sessionManager) {
        this.messageMapper = messageMapper;
        this.sessionManager = sessionManager;
    }

    @Override
    public void addMessage(String userId, Message content) {
        memory.put(userId, content);
        WebSocketSession session = sessionManager.getSession(userId);
        String conversationId = String.valueOf(session.getAttributes().get(WebSocketConstants.Conversation_Id));
        Assert.notNull(conversationId, "conversationId is null");

        if (content instanceof UserMessage userMessage) {
            messageMapper.insert(fun.aiboot.entity.Message.builder()
                    .conversationId(conversationId)
                    .sender("user")
                    .type("text")
                    .content(userMessage.getText())
                    .build());
        } else if (content instanceof AssistantMessage assistantMessage) {
            // todo : 大模型输出的内容比较多，后面考虑对上下文进行总结总结内容
            String text = assistantMessage.getText();
            messageMapper.insert(fun.aiboot.entity.Message.builder()
                    .conversationId(conversationId)
                    .sender("ai")
                    .type("text")
                    .content(text)
                    .build()
            );
        } else {
            throw new IllegalArgumentException("Unsupported message type: " + content.getClass().getName());
        }
    }

    // todo 查询建议应该对数据库中的内容进行总结优化处理
    @Override
    public List<Message> getMessages(String userId, Integer limit) {
        return new ArrayList<>();
    }

    @Override
    public void clearMessages(String userId) {

    }
}
