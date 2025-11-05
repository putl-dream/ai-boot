package fun.aiboot.dialogue.llm;

import reactor.core.publisher.Flux;


public interface LLMService {

    String chat(String userId, String modelId, String message);

    Flux<String> stream(String userId, String modelId, String message);

}
