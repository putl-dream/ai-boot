package fun.aiboot.dialogue.llm.tool;

import fun.aiboot.entity.Tool;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class GlobalToolRegistry implements ToolCallbackResolver {
    private static final String TAG = "FUNCTION_GLOBAL";

    // 用于存储所有function列表 (名称，工具)
    protected static final ConcurrentHashMap<String, ToolCallback> allFunction = new ConcurrentHashMap<>();
    // 用于存储所有permission列表 (权限，工具)
    protected static final ConcurrentHashMap<String, Tool> allPermission = new ConcurrentHashMap<>();

    @Resource
    protected List<GlobalTool> globalTools;

    @PostConstruct
    private void initTools() {
        for (GlobalTool function : globalTools) {
            ToolCallback toolCallback = function.getFunctionCallTool();
            String permission = function.getPermission();

            if (toolCallback == null || permission == null || permission.isEmpty()) {
                log.error("[{}] Function:{} is invalid", TAG, function.getClass().getSimpleName());
                continue;
            }

            registerFunction(toolCallback.getToolDefinition().name(), toolCallback);
            registerPermission(toolCallback, permission);
        }
        log.debug("[ {} ] 全局工具初始化完成", TAG);
    }


    public Map<String, ToolCallback> getAllFunctions() {
        return allFunction;
    }

    public Map<String, ToolCallback> getToolsByNames(List<String> names) {
        return allFunction.entrySet().stream()
                .filter(entry -> names.contains(entry.getKey()))
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, Tool> getAllPermission() {
        return allPermission;
    }


    @Override

    public ToolCallback resolve(@NotNull String toolName) {
        return allFunction.get(toolName);
    }

    /**
     * 按名称注册函数
     *
     * @param name             要注册的函数的名称
     * @param functionCallTool 要注册的函数
     */
    private void registerFunction(String name, ToolCallback functionCallTool) {
        allFunction.putIfAbsent(name, functionCallTool);
        log.debug("[{}] Function:{} registered into global successfully", TAG, name);
    }

    /**
     * 按名称注册权限
     *
     * @param toolCallback 要注册的函数
     * @param permission   要注册的权限
     */
    private void registerPermission(ToolCallback toolCallback, String permission) {
        ToolDefinition toolDefinition = toolCallback.getToolDefinition();
        Tool tool = Tool.builder()
                .name(toolDefinition.name())
                .type("tool")
                .config(toolDefinition.inputSchema())
                .description(toolDefinition.description())
                .build();
        allPermission.putIfAbsent(permission, tool);
        log.debug("[{}] 权限:{} 成功注册到全局", TAG, tool.getName());
    }


    /**
     * Unregister a impl by name
     *
     * @param name the name of the impl to unregister
     * @return true if successful, false otherwise
     */
    public boolean unregisterFunction(String name) {
        // Check if the impl exists before unregistering
        if (!allFunction.containsKey(name)) {
            log.error("[{}] Function:{} not found", TAG, name);
            return false;
        }
        allFunction.remove(name);
        log.info("[{}] Function:{} unregistered successfully", TAG, name);
        return true;
    }
}
