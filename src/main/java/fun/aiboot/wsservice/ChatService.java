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
        ChatMessage userMsg = (ChatMessage) message;

        // 生成唯一消息ID
        String messageId = UUID.randomUUID().toString();

        // 调用大模型服务
        Flux<String> stream = llmService.stream(userId, "default", userMsg.getContent());

        // 用于累积完整内容
        StringBuilder accumulated = new StringBuilder();

        // 启动流订阅（异步执行）
        stream
                .doOnNext(token -> {
                    accumulated.append(token);

                    ChatMessage chunkMessage = new ChatMessage(
                            "AI Assistant",
                            token, // 单个 token 增量
                            LocalDateTime.now(),
                            "text"
                    );
                    chunkMessage.setMessageId(messageId);
                    chunkMessage.setIsStreaming(true);
                    chunkMessage.setIsComplete(false);

                    // ✅ 发送流式片段
                    messagePublisher.sendToUser(userId, chunkMessage);
                })
                .doOnError(error -> {
                    log.error("流式响应出错 userId={} messageId={} err={}", userId, messageId, error.getMessage());

                    ChatMessage errorMsg = new ChatMessage(
                            "System",
                            "抱歉，生成响应时出现错误：" + error.getMessage(),
                            LocalDateTime.now(),
                            "system"
                    );
                    errorMsg.setMessageId(messageId);
                    errorMsg.setIsStreaming(false);
                    errorMsg.setIsComplete(true);
                    messagePublisher.sendToUser(userId, errorMsg);
                })
                .doOnComplete(() -> {
                    log.info("流式响应完成 userId={} messageId={} 总字数={}",
                            userId, messageId, accumulated.length());

                    ChatMessage completeMsg = new ChatMessage(
                            "AI Assistant",
                            accumulated.toString(),  // 全量内容
                            LocalDateTime.now(),
                            "text"
                    );
                    completeMsg.setMessageId(messageId);
                    completeMsg.setIsStreaming(false);
                    completeMsg.setIsComplete(true);

                    // ✅ 发送最终消息
                    messagePublisher.sendToUser(userId, completeMsg);
                })
                .subscribe(); // 启动流
    }
}

