package fun.aiboot.dialogue.llm.tool.impl;

import fun.aiboot.dialogue.llm.tool.GlobalTool;
import fun.aiboot.dialogue.llm.tool.GlobalToolResult;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Test1Tool implements GlobalTool {
    @Override
    public ToolCallback getFunctionCallTool() {
        return toolCallback;
    }

    @Override
    public String getPermission() {
        return "tool:huiben";
    }

    ToolCallback toolCallback = FunctionToolCallback
            .builder("func_playHuiBen", (Map<String, Integer> params, ToolContext toolContext) -> {
                Integer num = params.get("num");
                return "绘本《" + num + "》播放成功！";
            })
            .toolMetadata(ToolMetadata.builder().returnDirect(true).build())
            .description("绘本播放助手，需要用户提供绘本数字编号")
            .inputSchema("""
                        {
                            "type": "object",
                            "properties": {
                                "num": {
                                    "type": "integer",
                                    "description": "要播放的绘本数字编号"
                                }
                            },
                            "required": ["num"]
                        }
                    """)
            .inputType(Map.class)
            .toolCallResultConverter(GlobalToolResult.INSTANCE)
            .build();
}
