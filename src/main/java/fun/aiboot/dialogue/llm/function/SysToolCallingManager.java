package fun.aiboot.dialogue.llm.function;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.ai.tool.observation.DefaultToolCallingObservationConvention;
import org.springframework.ai.tool.observation.ToolCallingObservationContext;
import org.springframework.ai.tool.observation.ToolCallingObservationConvention;
import org.springframework.ai.tool.observation.ToolCallingObservationDocumentation;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Component
public class SysToolCallingManager implements ToolCallingManager {
    private static final ToolCallingObservationConvention DEFAULT_OBSERVATION_CONVENTION
            = new DefaultToolCallingObservationConvention();

    private final ObservationRegistry observationRegistry;

    private final ToolCallbackResolver toolCallbackResolver;

    private final ToolExecutionExceptionProcessor toolExecutionExceptionProcessor;

    private ToolCallingObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

    public SysToolCallingManager(ObservationRegistry observationRegistry, ToolCallbackResolver toolCallbackResolver,
                                 ToolExecutionExceptionProcessor toolExecutionExceptionProcessor) {
        Assert.notNull(observationRegistry, "observationRegistry cannot be null");
        Assert.notNull(toolCallbackResolver, "toolCallbackResolver cannot be null");
        Assert.notNull(toolExecutionExceptionProcessor, "toolCallExceptionConverter cannot be null");

        this.observationRegistry = observationRegistry;
        this.toolCallbackResolver = toolCallbackResolver;
        this.toolExecutionExceptionProcessor = toolExecutionExceptionProcessor;
    }


    @Override
    public List<ToolDefinition> resolveToolDefinitions(ToolCallingChatOptions chatOptions) {
        Assert.notNull(chatOptions, "chatOptions cannot be null");

        List<ToolCallback> toolCallbacks = new ArrayList<>(chatOptions.getToolCallbacks());
        for (String toolName : chatOptions.getToolNames()) {
            // Skip the function if it is already present in the request toolCallbacks.
            // That might happen if a function is defined in the options
            // both as a ToolCallback and as a function name.
            if (chatOptions.getToolCallbacks()
                    .stream()
                    .anyMatch(tool -> tool.getToolDefinition().name().equals(toolName))) {
                continue;
            }
            ToolCallback toolCallback = this.toolCallbackResolver.resolve(toolName);
            if (toolCallback == null) {
                throw new IllegalStateException("No ToolCallback found for function name: " + toolName);
            }
            toolCallbacks.add(toolCallback);
        }
        return toolCallbacks.stream().map(ToolCallback::getToolDefinition).toList();
    }

    @Override
    public ToolExecutionResult executeToolCalls(Prompt prompt, ChatResponse chatResponse) {
        Assert.notNull(prompt, "prompt cannot be null");
        Assert.notNull(chatResponse, "chatResponse cannot be null");

        // 查看需要执行的tool
        Optional<Generation> toolCallGeneration = chatResponse.getResults()
                .stream()
                .filter(g -> !CollectionUtils.isEmpty(g.getOutput().getToolCalls()))
                .findFirst();

        if (toolCallGeneration.isEmpty()) {
            throw new IllegalStateException("No function call requested by the chat model");
        }

        // 构建工具上下文
        List<Message> conversationHistory = new ArrayList<>(prompt.getInstructions());
        for (AssistantMessage.ToolCall toolCall : toolCallGeneration.get().getOutput().getToolCalls()) {
            AssistantMessage assistantMessage = toolCallGeneration.get().getOutput();
            Assert.notNull(assistantMessage.getText(), "AssistantMessage text cannot be null");

            AssistantMessage toolCallMessage = new AssistantMessage(assistantMessage.getText(), assistantMessage.getMetadata(), List.of(toolCall));
            ToolContext toolContext = buildToolContext(prompt, assistantMessage);
            SysToolCallingManager.InternalToolExecutionResult internalToolExecutionResult = executeToolCall(prompt, toolCallMessage, toolContext);

            ToolResponseMessage toolResponseMessage = internalToolExecutionResult.toolResponseMessage();
            conversationHistory.add(assistantMessage);
            conversationHistory.add(toolResponseMessage);
        }

        return ToolExecutionResult.builder()
                .conversationHistory(conversationHistory)
                .returnDirect(false)
                .build();
    }

    /**
     * 构建工具调用上下文
     * 目的： 构建工具调用上下文，这个上下文将用于工具调用。
     */
    private static ToolContext buildToolContext(Prompt prompt, AssistantMessage assistantMessage) {
        Map<String, Object> toolContextMap = Map.of();

        if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions
                && !CollectionUtils.isEmpty(toolCallingChatOptions.getToolContext())) {
            toolContextMap = new HashMap<>(toolCallingChatOptions.getToolContext());

            toolContextMap.put(ToolContext.TOOL_CALL_HISTORY,
                    buildConversationHistoryBeforeToolExecution(prompt, assistantMessage));
        }

        return new ToolContext(toolContextMap);
    }

    private static List<Message> buildConversationHistoryBeforeToolExecution(Prompt prompt,
                                                                             AssistantMessage assistantMessage) {
        List<Message> messageHistory = new ArrayList<>(prompt.copy().getInstructions());
        messageHistory.add(new AssistantMessage(assistantMessage.getText(), assistantMessage.getMetadata(),
                assistantMessage.getToolCalls()));
        return messageHistory;
    }

    /**
     * 执行工具调用并返回响应消息。
     */
    private SysToolCallingManager.InternalToolExecutionResult executeToolCall(Prompt prompt, AssistantMessage assistantMessage, ToolContext toolContext) {
        // 获取工具调用
        List<ToolCallback> toolCallbacks = List.of();
        if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions) {
            toolCallbacks = toolCallingChatOptions.getToolCallbacks();
        }

        // 创建工具响应
        List<ToolResponseMessage.ToolResponse> toolResponses = new ArrayList<>();
        for (AssistantMessage.ToolCall toolCall : assistantMessage.getToolCalls()) {

            String toolName = toolCall.name();
            String toolInputArguments = toolCall.arguments();

            ToolCallback toolCallback = toolCallbacks.stream()
                    .filter(tool -> toolName.equals(tool.getToolDefinition().name()))
                    .findFirst()
                    .orElseGet(() -> this.toolCallbackResolver.resolve(toolName));

            if (toolCallback == null) {
                throw new IllegalStateException("未找到工具名称的 ToolCallback: " + toolName);
            }

            ToolCallingObservationContext observationContext = ToolCallingObservationContext.builder()
                    .toolDefinition(toolCallback.getToolDefinition())
                    .toolMetadata(toolCallback.getToolMetadata())
                    .toolCallArguments(toolInputArguments)
                    .build();

            String toolCallResult = ToolCallingObservationDocumentation.TOOL_CALL
                    .observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
                            this.observationRegistry)
                    .observe(() -> {
                        String toolResult;
                        try {
                            toolResult = toolCallback.call(toolInputArguments, toolContext);
                        } catch (ToolExecutionException ex) {
                            toolResult = this.toolExecutionExceptionProcessor.process(ex);
                        }
                        observationContext.setToolCallResult(toolResult);
                        return toolResult;
                    });
            log.info("工具 [{}] 调用结果: {}", toolName, toolCallResult);
            toolResponses.add(new ToolResponseMessage.ToolResponse(toolCall.id(), toolName,
                    toolCallResult != null ? toolCallResult : ""));
        }

        return new SysToolCallingManager.InternalToolExecutionResult(new ToolResponseMessage(toolResponses, Map.of()), true);
    }


    private record InternalToolExecutionResult(ToolResponseMessage toolResponseMessage, boolean returnDirect) {
    }
}
