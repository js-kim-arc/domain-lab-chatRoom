package domainlab.chatroom.chat.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateChatRoomRequest(
        String topic,
        @NotBlank String name,
        @NotBlank String type
) {
}
