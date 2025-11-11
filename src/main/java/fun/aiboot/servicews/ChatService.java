package fun.aiboot.servicews;

import fun.aiboot.common.context.UserContext;
import fun.aiboot.dialogue.llm.LLMService;
import fun.aiboot.entity.Model;
import fun.aiboot.entity.ModelRole;
import fun.aiboot.service.ModelRoleService;
import fun.aiboot.service.ModelService;
import fun.aiboot.services.PermissionService;
import fun.aiboot.websocket.domain.BaseMessage;
import fun.aiboot.websocket.domain.ChatMessage;
import fun.aiboot.websocket.domain.FormType;
import fun.aiboot.websocket.server.MessageHandler;
import fun.aiboot.websocket.server.MessagePublisher;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ChatService implements MessageHandler {

    @Resource
    private LLMService llmService;
    @Resource
    private MessagePublisher messagePublisher;
    @Resource
    private PermissionService permissionService;
    @Resource
    private ModelService modelService;
    @Autowired
    private ModelRoleService modelRoleService;

    @Override
    public String getType() {
        return "chat";
    }

    @Override
    public void handleMessage(UserContext userContext, BaseMessage message) {
        String userId = userContext.getUserId();
        ChatMessage userMsg = (ChatMessage) message;

        // 生成唯一消息ID
        String messageId = UUID.randomUUID().toString();

        // 调用大模型服务
        String modelName = userContext.getCurrentModel();
        Model model = modelService.getByName(modelName);

        // 获取模型角色信息
        String modelRoleName = userContext.getCurrentModelRole();
        ModelRole modelRole = modelRoleService.getByName(modelRoleName);

        // 检查权限
        permissionService.hasModelIds(userId, List.of(modelName), false);
        Flux<String> stream = llmService.stream(userId, modelRole.getId(), userMsg.getContent());

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
                    ChatMessage completeMsg = ChatMessage.successEnd(FormType.SYSTEM, "生成响应完成");
                    messagePublisher.sendToUser(userId, completeMsg);
                })
                .subscribe(); // 启动流
    }
}

