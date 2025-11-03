package fun.aiboot.communication.interceptor;

import fun.aiboot.communication.server.WebSocketConstants;
import fun.aiboot.context.UserContext;
import fun.aiboot.utils.JwtUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = httpRequest.getHeader(WebSocketConstants.Auth_Header);
            String conversationId = httpRequest.getHeader(WebSocketConstants.Conversation_Id);

            // 如果token为空，抛出认证异常
            if (StringUtils.isEmpty(token) || StringUtils.isEmpty(conversationId)) {
                log.warn("WebSocket authentication failed: missing token or conversationId");
                response.setStatusCode(HttpStatusCode.valueOf(401));
                return false;
            }

            // 去除Bearer前缀（如果有）
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 解析token
            UserContext context = JwtUtil.parseJWT(token);
            if (context == null) {
                log.warn("WebSocket authentication failed: invalid token");
                response.setStatusCode(HttpStatusCode.valueOf(401));
            }

            // 将用户信息存储到attributes中,可以跨线程使用
            attributes.put(WebSocketConstants.User_Context, context);
            attributes.put(WebSocketConstants.Conversation_Id, conversationId);
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 上下文信息存入 WebSocketSession，不需要额外处理
    }
}
