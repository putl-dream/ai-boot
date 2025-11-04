package fun.aiboot.wsservice;

import fun.aiboot.communication.domain.BaseMessage;
import fun.aiboot.communication.domain.ChatMessage;
import fun.aiboot.communication.server.MessageHandler;
import fun.aiboot.communication.server.MessagePublisher;
import fun.aiboot.dialogue.llm.LLMService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class ChatService implements MessageHandler {

    @Resource
    private LLMService llmService;
    @Resource
    private MessagePublisher messagePublisher;

    @Override
    public String getType() {
        return "chat";
    }

    @Override
    public void handleMessage(String userId, BaseMessage message) {
        ChatMessage msg = (ChatMessage) message;
        Flux<String> stream = llmService.stream(userId, msg.getContent());

        // 生成唯一消息ID
        String messageId = UUID.randomUUID().toString();
        StringBuilder responseBuilder = new StringBuilder();

        stream.subscribe(
                chunk -> {
                    responseBuilder.append(chunk);
                    // 发送流式响应片段
                    ChatMessage chatMessage = new ChatMessage(
                            "AI Assistant",  // 或者从配置读取
                            chunk,  // 发送累积的完整内容
                            LocalDateTime.now(),
                            "text"
                    );
                    chatMessage.setMessageId(messageId);
                    chatMessage.setIsStreaming(true);
                    chatMessage.setIsComplete(false);
                    messagePublisher.sendToUser(userId, chatMessage);
                },
                error -> {
                    log.error("Error in streaming: {}", error.getMessage());
                    // 发送错误消息
                    ChatMessage errorMessage = new ChatMessage(
                            "System",
                            "抱歉，消息发送失败：" + error.getMessage(),
                            LocalDateTime.now(),
                            "system"
                    );
                    errorMessage.setMessageId(messageId);
                    errorMessage.setIsComplete(true);
                    messagePublisher.sendToUser(userId, errorMessage);
                },
                () -> {
                    log.info("Stream completed response：\n{}", responseBuilder);
                    // 发送完成标记
                    ChatMessage completeMessage = new ChatMessage(
                            "AI Assistant",
                            responseBuilder.toString(),
                            LocalDateTime.now(),
                            "text"
                    );
                    completeMessage.setMessageId(messageId);
                    completeMessage.setIsStreaming(false);
                    completeMessage.setIsComplete(true);
                    messagePublisher.sendToUser(userId, completeMessage);
                }
        );
    }
}
