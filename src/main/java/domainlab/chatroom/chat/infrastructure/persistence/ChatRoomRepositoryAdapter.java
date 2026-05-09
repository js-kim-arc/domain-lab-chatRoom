package domainlab.chatroom.chat.infrastructure.persistence;

import domainlab.chatroom.chat.domain.model.ChatRoom;
import domainlab.chatroom.chat.domain.repository.ChatRoomRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ChatRoomRepositoryAdapter implements ChatRoomRepository {

    private final ChatRoomJpaRepository jpa;

    public ChatRoomRepositoryAdapter(ChatRoomJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        return jpa.save(chatRoom);
    }

    @Override
    public Optional<ChatRoom> findById(Long id) {
        return jpa.findById(id);
    }
}
