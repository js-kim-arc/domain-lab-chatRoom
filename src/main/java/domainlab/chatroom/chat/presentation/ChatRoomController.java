package domainlab.chatroom.chat.presentation;

import domainlab.chatroom.chat.application.service.ChatRoomCommandService;
import domainlab.chatroom.chat.domain.model.ChatRoom;
import domainlab.chatroom.chat.presentation.dto.CreateChatRoomRequest;
import domainlab.chatroom.chat.presentation.dto.CreateChatRoomResponse;
import domainlab.chatroom.common.auth.CurrentUserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat-rooms")
public class ChatRoomController {

    private static final int CREATOR_ONLY_MEMBER_COUNT = 1;

    private final ChatRoomCommandService chatRoomCommandService;

    public ChatRoomController(ChatRoomCommandService chatRoomCommandService) {
        this.chatRoomCommandService = chatRoomCommandService;
    }

    @PostMapping
    public ResponseEntity<CreateChatRoomResponse> create(
            @CurrentUserId Long currentUserId,
            @Valid @RequestBody CreateChatRoomRequest request) {
        ChatRoom room = chatRoomCommandService.createChatRoom(
                request.topic(), request.name(), request.type(), currentUserId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateChatRoomResponse.from(room, CREATOR_ONLY_MEMBER_COUNT));
    }
}
