package fun.aiboot.common.interceptor;

import fun.aiboot.common.context.UserContext;
import fun.aiboot.dialogue.llm.config.LlmModelConfiguration;
import fun.aiboot.dialogue.llm.context.ModelConfigContext;
import fun.aiboot.entity.Model;
import fun.aiboot.entity.ModelRole;
import fun.aiboot.service.ModelRoleService;
import fun.aiboot.service.ModelService;
import fun.aiboot.service.RoleToolService;
import fun.aiboot.services.PermissionService;
import fun.aiboot.utils.JwtUtil;
import fun.aiboot.websocket.server.WebSocketConstants;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
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
    private ModelRoleService modelRoleService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private RoleToolService roleToolService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }

        try {
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
                return false;
            }

            // 权限校验
            boolean p1 = permissionService.hasRoleName(context.getUserId(), context.getRoleNames(), true);
            boolean p2 = permissionService.hasModelIds(context.getUserId(), context.getRoleModelIds(), true);
            boolean p3 = permissionService.hasToolIds(context.getUserId(), context.getRoleToolIds(), true);

            if (!p1 || !p2 || !p3) {
                log.warn("WebSocket authentication failed: user does not have permission");
                response.setStatusCode(HttpStatusCode.valueOf(403));
                return false;
            }

            ModelRole modelRole = modelRoleService.getByName(context.getCurrentModelRole());
            Model modelInfo = modelService.getByName(context.getCurrentModel());
            List<String> userTools = roleToolService.selectToolNameByRoleIds(context.getRoleNames());

            modelConfigContext.save(LlmModelConfiguration.builder()
                    .id(modelRole.getId())
                    .modelName(modelInfo.getName())
                    .apiKey(modelInfo.getModelKey())
                    .provider(modelInfo.getProvider())
                    .roleName(modelRole.getName())
                    .exposedTools(userTools)
                    .build());

            // 将用户信息存储到attributes中,可以跨线程使用
            attributes.put(WebSocketConstants.User_Context, context);
            attributes.put(WebSocketConstants.Conversation_Id, conversationId);
            log.debug("[ {} ] 用户身份验证通过", context.getUsername());
        } catch (Exception e) {
            log.error("token解析失败：{}", e.getMessage());
            response.setStatusCode(HttpStatusCode.valueOf(401));
            return false;
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 上下文信息存入 WebSocketSession，不需要额外处理
    }
}
