package domainlab.chatroom.chat.presentation.dto;

import domainlab.chatroom.chat.domain.model.ChatRoom;
import domainlab.chatroom.chat.domain.model.ChatRoomType;

import java.time.LocalDateTime;

public record CreateChatRoomResponse(
        Long roomId,
        String topic,
        String name,
        ChatRoomType type,
        Long createdBy,
        int memberCount,
        LocalDateTime createdAt
) {

    public static CreateChatRoomResponse from(ChatRoom room, int memberCount) {
        return new CreateChatRoomResponse(
                room.getId(),
                room.getTopic().value(),
                room.getName(),
                room.getType(),
                room.getCreatedBy(),
                memberCount,
                room.getCreatedAt());
    }
}
