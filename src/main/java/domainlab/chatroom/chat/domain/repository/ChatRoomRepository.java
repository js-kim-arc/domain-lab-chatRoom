package domainlab.chatroom.chat.domain.repository;

import domainlab.chatroom.chat.domain.model.ChatRoom;

import java.util.Optional;

public interface ChatRoomRepository {

    ChatRoom save(ChatRoom chatRoom);

    Optional<ChatRoom> findById(Long id);
}
