package fun.aiboot.dialogue.llm.function;

import com.alibaba.fastjson2.JSON;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.tool.execution.ToolCallResultConverter;

import java.lang.reflect.Type;

/**
 * 函数返回结果转换器
 */
public class ToolCallStringResultConverter implements ToolCallResultConverter {

    public static final ToolCallStringResultConverter INSTANCE = new ToolCallStringResultConverter();

    private ToolCallStringResultConverter() {
        // Private constructor to enforce singleton pattern
    }

    @NotNull
    @Override
    public String convert(Object result, Type returnType) {
        return JSON.toJSONString(result);
    }
}
