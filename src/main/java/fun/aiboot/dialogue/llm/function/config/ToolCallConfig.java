package fun.aiboot.dialogue.llm.function.config;

import io.micrometer.observation.ObservationRegistry;
import fun.aiboot.dialogue.llm.function.SysToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolCallConfig {

    private final ToolExecutionExceptionProcessor defaultToolExecutionExceptionProcessor
            = DefaultToolExecutionExceptionProcessor.builder().build();

    @Bean
    public ToolCallingManager toolCallingManager(ObservationRegistry observationRegistry, ToolCallbackResolver toolCallbackResolver,
                                                 @Autowired(required = false) ToolExecutionExceptionProcessor toolExecutionExceptionProcessor) {
        // Create and return a ToolCallingManager instance
        // This is a placeholder; actual implementation may vary based on requirements
        return new SysToolCallingManager(observationRegistry, toolCallbackResolver,
                toolExecutionExceptionProcessor == null ? defaultToolExecutionExceptionProcessor : toolExecutionExceptionProcessor);
    }
}
