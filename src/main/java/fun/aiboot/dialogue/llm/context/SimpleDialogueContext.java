package fun.aiboot.dialogue.llm.context;

import org.springframework.ai.chat.messages.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleDialogueContext implements DialogueContext {

    private final Map<String, List<Message>> memory = new HashMap<>();

    @Override
    public void addMessage(String userId, Message content) {
        List<Message> messages = memory.get(userId);
        if (messages == null) {
            messages = new ArrayList<>();
            memory.put(userId, messages);
        } else {
            messages.add(content);
        }

        memory.forEach((key, value) -> System.out.println("UserId: " + key + " Messages: " + value));
    }

    @Override
    public List<Message> getMessages(String userId, Integer limit) {
        List<Message> messages = memory.get(userId);
        if (messages == null) {
            return new ArrayList<>();
        }
        return messages.subList(Math.max(0, messages.size() - limit), messages.size());
    }

    @Override
    public void clearMessages(String userId) {
        memory.remove(userId);
    }
}
