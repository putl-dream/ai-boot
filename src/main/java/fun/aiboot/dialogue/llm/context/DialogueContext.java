package fun.aiboot.dialogue.llm.context;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 暂时放弃spring ai 的ChatMemory。
 */
public interface DialogueContext {

    /**
     * 添加消息
     */
    void addMessage(String userId, Message content);

    /**
     * 获取历史对话消息列表
     */
    List<Message> getMessages(String userId, Integer limit);

    /**
     * 清除历史记录
     */
    void clearMessages(String userId);

}