package fun.aiboot.dialogue.llm.function.function;

import fun.aiboot.dialogue.llm.function.GlobalFunction;
import fun.aiboot.dialogue.llm.function.ToolCallStringResultConverter;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TestFunction implements GlobalFunction {
    @Override
    public ToolCallback getFunctionCallTool() {
        return FunctionToolCallback.builder("func_weather", (Map<String, String> params, ToolContext toolContext) -> {
                    try {
                        String city = params.get("city");
                        return Map.of("city", city, "weather", "晴天", "temperature", "25°C");
                    } catch (Exception e) {
                        return "获取天气信息失败！";
                    }
                }).toolMetadata(ToolMetadata.builder().returnDirect(true).build())
                .description("立即查询指定城市的当前天气。当用户提到任何城市天气、气温、是否下雨等，必须调用此工具。")
                .inputSchema("""
                        {
                            "type": "object",
                            "properties": {
                                "city": {
                                    "type": "string",
                                    "description": "要查询天气的城市名字"
                                }
                            },
                            "required": ["city"]
                        }
                        """).inputType(Map.class)
                .toolCallResultConverter(ToolCallStringResultConverter.INSTANCE)
                .build();
    }
}
