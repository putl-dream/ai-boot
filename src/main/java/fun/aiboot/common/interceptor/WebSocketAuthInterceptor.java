package fun.aiboot.common.interceptor;

import fun.aiboot.websocket.server.WebSocketConstants;
import fun.aiboot.common.context.UserContext;
import fun.aiboot.dialogue.llm.context.ModelConfigContext;
import fun.aiboot.dialogue.llm.config.LlmModelConfiguration;
import fun.aiboot.entity.Model;
import fun.aiboot.entity.SysPrompt;
import fun.aiboot.service.SysPromptService;
import fun.aiboot.services.PermissionService;
import fun.aiboot.utils.JwtUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    @Resource
    private ModelConfigContext modelConfigContext;
    @Resource
    private PermissionService permissionService;
    @Resource
    private SysPromptService sysPromptService;

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

            Assert.notNull(context, "User information cannot be empty");
            SysPrompt sysPrompt = sysPromptService.getById(context.getCurrentModelId());

            // 获取模型配置（modelname、key）,随机获取一个
            Model randomModel = permissionService.getRandomModel(context.getUserId());
            List<String> userTools = permissionService.getUserTools(context.getUserId());

            modelConfigContext.save(LlmModelConfiguration.builder()
                    .id(sysPrompt.getId())
                    .modelName(randomModel.getName())
                    .apiKey(randomModel.getModelKey())
                    .provider(randomModel.getProvider())
                    .roleName(sysPrompt.getName())
                    .exposedTools(userTools)
                    .build());

            // 将用户信息存储到attributes中,可以跨线程使用
            attributes.put(WebSocketConstants.User_Context, context);
            attributes.put(WebSocketConstants.Conversation_Id, conversationId);
            Assert.notNull(context, "User information cannot be empty");
            log.info("【{}】The user identity verification has been passed", context.getUsername());
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
