package fun.aiboot.websocket.domain;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChatMessage.class, name = "chat"),
})
public sealed abstract class BaseMessage permits ChatMessage {
    protected String type;   // 消息类型

    public BaseMessage(String type) {
        this.type = type;
    }

    public BaseMessage() {
        this.type = "unknown";
    }
}
