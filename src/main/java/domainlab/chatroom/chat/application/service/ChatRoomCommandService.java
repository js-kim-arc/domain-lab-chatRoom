package domainlab.chatroom.chat.application.service;

import domainlab.chatroom.chat.domain.model.ChatRoom;
import domainlab.chatroom.chat.domain.model.ChatRoomType;
import domainlab.chatroom.chat.domain.model.Membership;
import domainlab.chatroom.chat.domain.model.Topic;
import domainlab.chatroom.chat.domain.repository.ChatRoomRepository;
import domainlab.chatroom.chat.domain.repository.MembershipRepository;
import domainlab.chatroom.chat.exception.ChatRoomDomainException;
import domainlab.chatroom.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatRoomCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final MembershipRepository membershipRepository;

    public ChatRoomCommandService(ChatRoomRepository chatRoomRepository,
                                  MembershipRepository membershipRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public ChatRoom createChatRoom(String rawTopic, String name, String type, Long currentUserId) {
        Topic topic = Topic.of(rawTopic);
        ChatRoomType chatRoomType = parseType(type);

        ChatRoom room = ChatRoom.create(topic, name, chatRoomType, currentUserId);
        chatRoomRepository.save(room);

        Membership creatorMembership = Membership.create(currentUserId, room.getId());
        membershipRepository.save(creatorMembership);

        return room;
    }

    private ChatRoomType parseType(String type) {
        if (type == null || type.isBlank()) {
            throw ChatRoomDomainException.of(ErrorCode.C001);
        }
        try {
            return ChatRoomType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw ChatRoomDomainException.of(ErrorCode.C001);
        }
    }
}
