package domainlab.chatroom.chat.domain.model;

import domainlab.chatroom.chat.exception.TopicDomainException;
import domainlab.chatroom.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Topic {

    private static final int MAX_LENGTH = 100;

    @Column(name = "topic", nullable = false, length = MAX_LENGTH)
    private String value;

    protected Topic() {
    }

    private Topic(String value) {
        this.value = value;
    }

    public static Topic of(String rawValue) {
        if (rawValue == null) {
            throw TopicDomainException.of(ErrorCode.TP001);
        }
        String normalized = normalize(rawValue);
        if (normalized.isEmpty()) {
            throw TopicDomainException.of(ErrorCode.TP002);
        }
        return new Topic(normalized);
    }

    private static String normalize(String raw) {
        String trimmed = raw.trim();
        String lowered = trimmed.toLowerCase();
        String filtered = lowered.codePoints()
                .filter(Topic::isAllowed)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        String dashed = filtered.replaceAll("\\s+", "-");
        String compacted = dashed.replaceAll("-+", "-");
        return compacted.replaceAll("^-+|-+$", "");
    }

    private static boolean isAllowed(int cp) {
        if (Character.isWhitespace(cp)) {
            return true;
        }
        if (cp == '-') {
            return true;
        }
        if (cp >= 'a' && cp <= 'z') {
            return true;
        }
        if (cp >= '0' && cp <= '9') {
            return true;
        }
        if (cp >= 0xAC00 && cp <= 0xD7A3) {
            return true;
        }
        if (cp >= 0x1100 && cp <= 0x11FF) {
            return true;
        }
        if (cp >= 0x3130 && cp <= 0x318F) {
            return true;
        }
        return false;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Topic other)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
