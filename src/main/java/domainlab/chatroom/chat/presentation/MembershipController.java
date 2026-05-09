package domainlab.chatroom.chat.presentation;

import domainlab.chatroom.chat.application.service.JoinChatRoomResult;
import domainlab.chatroom.chat.application.service.MembershipCommandService;
import domainlab.chatroom.chat.presentation.dto.JoinChatRoomResponse;
import domainlab.chatroom.common.auth.CurrentUserId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat-rooms")
public class MembershipController {

    private final MembershipCommandService membershipCommandService;

    public MembershipController(MembershipCommandService membershipCommandService) {
        this.membershipCommandService = membershipCommandService;
    }

    @PostMapping("/{roomId}/memberships")
    public ResponseEntity<JoinChatRoomResponse> join(
            @CurrentUserId Long currentUserId,
            @PathVariable Long roomId) {
        JoinChatRoomResult result = membershipCommandService.joinChatRoom(currentUserId, roomId);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(JoinChatRoomResponse.from(result.membership()));
    }
}
