package fun.aiboot.dialogue.llm.function;

import org.springframework.ai.tool.ToolCallback;

public interface GlobalFunction {
    ToolCallback getFunctionCallTool();
}