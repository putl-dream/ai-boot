package fun.aiboot.websocket.server;

import com.alibaba.fastjson2.JSON;
import fun.aiboot.websocket.domain.BaseMessage;
import fun.aiboot.websocket.domain.ChatMessage;
import fun.aiboot.websocket.domain.FormType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DefaultMessageRouter implements MessageRouter {

    private final Map<String, MessageHandler> handlers = new HashMap<>();
    @Resource
    private MessagePublisher messagePublisher;

    @Autowired
    public DefaultMessageRouter(List<MessageHandler> handlerList) {
        handlerList.forEach(handler -> handlers.put(handler.getType(), handler));
    }

    @Override
    public void route(String userId, String rawMessage) {
        try {
            // 解析消息结构
            if (rawMessage.equals("ping")) {
                log.info("收到ping消息");
                return;
            }

            var base = JSON.to(BaseMessage.class, rawMessage);
            String type = base.getType();

            MessageHandler handler = handlers.get(type);
            if (handler == null) {
                log.warn("未找到该消息类型的处理程序: {}", type);
                return;
            }

            handler.handleMessage(userId, base);
        } catch (Exception e) {
            log.error("处理消息时发生错误: {}", e.getMessage(), e);
            ChatMessage errorMsg = ChatMessage.successEnd(FormType.SYSTEM, "生成响应时出现错误：" + e.getMessage());
            messagePublisher.sendToUser(userId, errorMsg);
        }
    }
}
