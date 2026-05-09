package domainlab.chatroom.chat.domain.model;

import domainlab.chatroom.chat.exception.ChatRoomDomainException;
import domainlab.chatroom.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
public class ChatRoom {

    private static final int NAME_MAX_LENGTH = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Topic topic;

    @Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private ChatRoomType type;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ChatRoom() {
    }

    private ChatRoom(Topic topic, String name, ChatRoomType type, Long createdBy, LocalDateTime now) {
        this.topic = topic;
        this.name = name;
        this.type = type;
        this.createdBy = createdBy;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static ChatRoom create(Topic topic, String name, ChatRoomType type, Long createdBy) {
        validateTopic(topic);
        String trimmedName = validateAndTrimName(name);
        validateType(type);
        validateCreatedBy(createdBy);
        return new ChatRoom(topic, trimmedName, type, createdBy, LocalDateTime.now());
    }

    public void changeName(String newName) {
        String trimmed = validateAndTrimName(newName);
        this.name = trimmed;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOpen() {
        return type == ChatRoomType.OPEN;
    }

    private static void validateTopic(Topic topic) {
        if (topic == null) {
            throw ChatRoomDomainException.of(ErrorCode.TP001);
        }
    }

    private static String validateAndTrimName(String name) {
        if (name == null) {
            throw ChatRoomDomainException.of(ErrorCode.C001);
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw ChatRoomDomainException.of(ErrorCode.C001);
        }
        if (trimmed.length() > NAME_MAX_LENGTH) {
            throw ChatRoomDomainException.of(ErrorCode.CR002);
        }
        return trimmed;
    }

    private static void validateType(ChatRoomType type) {
        if (type == null) {
            throw ChatRoomDomainException.of(ErrorCode.C001);
        }
        if (type != ChatRoomType.OPEN) {
            throw ChatRoomDomainException.of(ErrorCode.CR003);
        }
    }

    private static void validateCreatedBy(Long createdBy) {
        if (createdBy == null) {
            throw ChatRoomDomainException.of(ErrorCode.C001);
        }
    }

    public Long getId() {
        return id;
    }

    public Topic getTopic() {
        return topic;
    }

    public String getName() {
        return name;
    }

    public ChatRoomType getType() {
        return type;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
