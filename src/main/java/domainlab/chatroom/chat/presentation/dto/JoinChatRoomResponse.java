package domainlab.chatroom.chat.presentation.dto;

import domainlab.chatroom.chat.domain.model.Membership;
import domainlab.chatroom.chat.domain.model.MembershipStatus;

import java.time.LocalDateTime;

public record JoinChatRoomResponse(
        Long membershipId,
        Long userId,
        Long roomId,
        MembershipStatus status,
        LocalDateTime joinedAt,
        LocalDateTime leftAt
) {

    public static JoinChatRoomResponse from(Membership m) {
        return new JoinChatRoomResponse(
                m.getId(),
                m.getUserId(),
                m.getRoomId(),
                m.getStatus(),
                m.getJoinedAt(),
                m.getLeftAt());
    }
}
