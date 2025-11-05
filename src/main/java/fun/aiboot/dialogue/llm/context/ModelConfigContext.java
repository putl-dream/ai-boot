package fun.aiboot.dialogue.llm.context;

import fun.aiboot.dialogue.llm.config.LlmModelConfiguration;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ModelConfigContext {
    private final Map<String, LlmModelConfiguration> store = new ConcurrentHashMap<>();

    public void save(LlmModelConfiguration cfg) {
        store.put(cfg.getId(), cfg);
    }

    public LlmModelConfiguration get(String id) {
        return store.get(id);
    }

    public java.util.Collection<LlmModelConfiguration> list() {
        return store.values();
    }
}