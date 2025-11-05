package fun.aiboot.dialogue.llm.config;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LlmModelConfiguration {
    private String id;
    private String modelName;
    private String roleName;
    private String provider;
    private String apiKey;

    private List<String> exposedTools;
}
