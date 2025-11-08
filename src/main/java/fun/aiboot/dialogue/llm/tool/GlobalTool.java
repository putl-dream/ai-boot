package fun.aiboot.dialogue.llm.tool;

import org.springframework.ai.tool.ToolCallback;

/**
 * 全局函数统一接口
 */
public interface GlobalTool {
    /**
     * 获取函数调用工具
     *
     * @return 工具调用
     */
    ToolCallback getFunctionCallTool();


    /**
     * 获取函数权限
     *
     * @return 权限
     */
    String getPermission();

}