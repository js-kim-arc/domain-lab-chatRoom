package domainlab.chatroom.chat.infrastructure.persistence;

import domainlab.chatroom.chat.domain.model.ChatRoom;
import domainlab.chatroom.chat.domain.model.ChatRoomType;
import domainlab.chatroom.chat.domain.model.Topic;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatRoomJpaRepositoryTest {

    @Autowired
    private ChatRoomJpaRepository repository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("저장 후 findById로 조회된다")
    void findById_저장후조회() {
        ChatRoom room = ChatRoom.create(Topic.of("Spring Boot"), "스프링 스터디", ChatRoomType.OPEN, 1L);

        ChatRoom saved = repository.saveAndFlush(room);

        Optional<ChatRoom> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTopic().value()).isEqualTo("spring-boot");
        assertThat(found.get().getName()).isEqualTo("스프링 스터디");
        assertThat(found.get().getType()).isEqualTo(ChatRoomType.OPEN);
        assertThat(found.get().getCreatedBy()).isEqualTo(1L);
    }

    @Test
    @DisplayName("저장하지 않은 id로 조회하면 Optional.empty()")
    void findById_미존재_empty() {
        assertThat(repository.findById(99_999L)).isEmpty();
    }

    @Test
    @DisplayName("createdAt이 저장 시 도메인 시각 그대로 보존된다 (microsecond 정밀도)")
    void createdAt_저장시자동기록() {
        ChatRoom room = ChatRoom.create(Topic.of("t"), "n", ChatRoomType.OPEN, 1L);
        var preserved = room.getCreatedAt();

        ChatRoom saved = repository.saveAndFlush(room);
        em.clear();

        ChatRoom found = repository.findById(saved.getId()).orElseThrow();
        assertThat(found.getCreatedAt())
                .isEqualToIgnoringNanos(preserved.truncatedTo(ChronoUnit.MICROS));
    }
}
