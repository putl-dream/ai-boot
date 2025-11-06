package fun.aiboot.communication.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public final class ChatMessage extends BaseMessage {
    public ChatMessage() {
        super("chat");
    }

    private String from;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;
    private String msgType;

    public ChatMessage(String from, String content, LocalDateTime time, String msgType) {
        super("chat");
        this.from = from;
        this.content = content;
        this.time = time;
        this.msgType = msgType;
    }

    private String messageId;  // 消息唯一标识
    private Boolean isStreaming; // 是否是流式消息
    private Boolean isComplete;  // 流是否完成

    public static ChatMessage successEnd(FormType from, String content) {
        return new ChatMessage(from.toString(), content, LocalDateTime.now(), "text", UUID.randomUUID().toString(), false, true);
    }

    public static ChatMessage success(FormType from, String content) {
        return new ChatMessage(from.toString(), content, LocalDateTime.now(), "text", UUID.randomUUID().toString(), true, false);
    }

}
