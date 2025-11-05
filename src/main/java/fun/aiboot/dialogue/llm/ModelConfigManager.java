package fun.aiboot.dialogue.llm;

import fun.aiboot.dialogue.llm.config.ModelConfig;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ModelConfigManager {
    private final Map<String, ModelConfig> store = new ConcurrentHashMap<>();

    public void save(ModelConfig cfg) {
        store.put(cfg.getId(), cfg);
    }

    public ModelConfig get(String id) {
        return store.get(id);
    }

    public java.util.Collection<ModelConfig> list() {
        return store.values();
    }
}