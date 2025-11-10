package fun.aiboot.websocket.config;


import fun.aiboot.common.interceptor.WebSocketAuthInterceptor;
import fun.aiboot.websocket.server.WebSocketHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Slf4j
@Configuration
@EnableWebSocket  // 开启STOMP协议的WebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Value("${socket.path:/ws}")
    private String WS_PATH;

    @Resource
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Resource
    private WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, WS_PATH)
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");

        log.info("[ WebSocketPath ] 已注册 WebSocket 端点: {}", WS_PATH);
        log.info("=================================[ WebSocket 初始化成功 ]==========================================");
    }
}
