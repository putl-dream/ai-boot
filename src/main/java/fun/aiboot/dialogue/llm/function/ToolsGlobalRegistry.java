package fun.aiboot.dialogue.llm.function;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ToolsGlobalRegistry implements ToolCallbackResolver {
    private static final String TAG = "FUNCTION_GLOBAL";

    // 用于存储所有function列表
    protected static final ConcurrentHashMap<String, ToolCallback> allFunction
            = new ConcurrentHashMap<>();

    @Resource
    protected List<GlobalFunction> globalFunctions;

    @Override
    public ToolCallback resolve(@NotNull String toolName) {
        return allFunction.get(toolName);
    }

    /**
     * Register a function by name
     *
     * @param name the name of the function to register
     * @return the registered function or null if not found
     */
    public ToolCallback registerFunction(String name, ToolCallback functionCallTool) {
        ToolCallback result = allFunction.putIfAbsent(name, functionCallTool);
        log.debug("[{}] Function:{} registered into global successfully", TAG, name);
        return result;
    }

    /**
     * Unregister a function by name
     *
     * @param name the name of the function to unregister
     * @return true if successful, false otherwise
     */
    public boolean unregisterFunction(String name) {
        // Check if the function exists before unregistering
        if (!allFunction.containsKey(name)) {
            log.error("[{}] Function:{} not found", TAG, name);
            return false;
        }
        allFunction.remove(name);
        log.info("[{}] Function:{} unregistered successfully", TAG, name);
        return true;
    }

    /**
     * Get all registered functions
     *
     * @return a map of all registered functions
     */
    public Map<String, ToolCallback> getAllFunctions() {
        globalFunctions.forEach(
                globalFunction -> {
                    ToolCallback toolCallback = globalFunction.getFunctionCallTool();
                    if (toolCallback != null) {
                        registerFunction(toolCallback.getToolDefinition().name(),
                                toolCallback);
                    }
                }
        );
        return allFunction;
    }
}
