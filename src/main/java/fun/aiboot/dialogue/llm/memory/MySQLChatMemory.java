package fun.aiboot.dialogue.llm.memory;

import fun.aiboot.communication.server.SessionManager;
import fun.aiboot.mapper.MessageMapper;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MySQLChatMemory implements ChatMemory {


    private final Map<String, Message> memory = new HashMap<>();

    @Resource
    private MessageMapper messageMapper;
    @Resource
    private SessionManager sessionManager;

    @Override
    public void addMessage(String userId, Message content) {
        memory.put(userId, content);
        if (content instanceof UserMessage userMessage) {
            messageMapper.insert(fun.aiboot.entity.Message.builder()
                    .conversationId(sessionManager.getSessionId(userId))
                    .sender("user")
                    .type("text")
                    .content(userMessage.getText())
                    .build());
        } else if (content instanceof AssistantMessage assistantMessage) {
            // todo : 大模型输出的内容比较多，后面考虑对上下文进行总结总结内容
            String text = assistantMessage.getText();
            messageMapper.insert(
                    fun.aiboot.entity.Message.builder()
                            .conversationId(sessionManager.getSession(userId).getId())
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
