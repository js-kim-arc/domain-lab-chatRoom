package domainlab.chatroom.chat.application.service;

import domainlab.chatroom.chat.domain.model.Membership;

public record JoinChatRoomResult(Membership membership, boolean created) {
}
