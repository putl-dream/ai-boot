package fun.aiboot.servicews;

import fun.aiboot.communication.domain.BaseMessage;
import fun.aiboot.communication.domain.ChatMessage;
import fun.aiboot.communication.domain.FormType;
import fun.aiboot.communication.server.MessageHandler;
import fun.aiboot.communication.server.MessagePublisher;
import fun.aiboot.dialogue.llm.LLMService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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

        // 启动流订阅（异步执行）
        stream
                .doOnNext(token -> {
                    ChatMessage chunkMessage = ChatMessage.success(FormType.ASSISTANT, token);
                    messagePublisher.sendToUser(userId, chunkMessage);
                })
                .doOnError(error -> {
                    log.error("流式响应出错 userId={} messageId={} err={}", userId, messageId, error.getMessage());
                    ChatMessage errorMsg = ChatMessage.successEnd(FormType.SYSTEM, "生成响应时出现错误：" + error.getMessage());
                    messagePublisher.sendToUser(userId, errorMsg);
                })
                .doOnComplete(() -> {
                    log.info("流式响应完成 userId={} messageId={}", userId, messageId);
                    ChatMessage completeMsg = ChatMessage.successEnd(FormType.SYSTEM, "生成响应完成");
                    messagePublisher.sendToUser(userId, completeMsg);
                })
                .subscribe(); // 启动流
    }
}

