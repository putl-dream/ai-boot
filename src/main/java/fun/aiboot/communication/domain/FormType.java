package fun.aiboot.communication.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum FormType {
    SYSTEM("system"), USER("user"), ASSISTANT("assistant");

    private final String value;

    @Override
    public String toString() {
        return this.value;
    }
}
