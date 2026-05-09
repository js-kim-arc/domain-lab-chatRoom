package domainlab.chatroom.chat.infrastructure.persistence;

import domainlab.chatroom.chat.domain.model.ChatRoom;
import domainlab.chatroom.chat.domain.model.ChatRoomType;
import domainlab.chatroom.chat.domain.model.Membership;
import domainlab.chatroom.chat.domain.model.MembershipStatus;
import domainlab.chatroom.chat.domain.model.Topic;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MembershipJpaRepositoryTest {

    @Autowired
    private MembershipJpaRepository membershipRepository;

    @Autowired
    private ChatRoomJpaRepository chatRoomRepository;

    @Autowired
    private EntityManager em;

    private Long roomId;

    @BeforeEach
    void setUpChatRoom() {
        ChatRoom room = ChatRoom.create(Topic.of("topic"), "name", ChatRoomType.OPEN, 1L);
        roomId = chatRoomRepository.saveAndFlush(room).getId();
    }

    private Membership saveActive(Long userId, Long roomId) {
        return membershipRepository.saveAndFlush(Membership.create(userId, roomId));
    }

    private Membership saveLeft(Long userId, Long roomId) {
        Membership m = membershipRepository.saveAndFlush(Membership.create(userId, roomId));
        m.leave();
        return membershipRepository.saveAndFlush(m);
    }

    @Nested
    @DisplayName("findByUserIdAndRoomIdAndStatus")
    class FindBy {

        @Test
        @DisplayName("ACTIVE 멤버십 저장 후 ACTIVE로 조회하면 present")
        void findByUserIdAndRoomIdAndStatus_ACTIVE조회() {
            saveActive(1L, roomId);

            Optional<Membership> found = membershipRepository
                    .findByUserIdAndRoomIdAndStatus(1L, roomId, MembershipStatus.ACTIVE);

            assertThat(found).isPresent();
            assertThat(found.get().getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        }

        @Test
        @DisplayName("LEFT 상태 저장 후 ACTIVE로 조회하면 empty")
        void findByUserIdAndRoomIdAndStatus_LEFT_empty() {
            saveLeft(1L, roomId);

            Optional<Membership> found = membershipRepository
                    .findByUserIdAndRoomIdAndStatus(1L, roomId, MembershipStatus.ACTIVE);

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("userId가 다르면 empty")
        void findByUserIdAndRoomIdAndStatus_userId다름_empty() {
            saveActive(1L, roomId);

            Optional<Membership> found = membershipRepository
                    .findByUserIdAndRoomIdAndStatus(2L, roomId, MembershipStatus.ACTIVE);

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("roomId가 다르면 empty")
        void findByUserIdAndRoomIdAndStatus_roomId다름_empty() {
            saveActive(1L, roomId);

            ChatRoom otherRoom = ChatRoom.create(Topic.of("other"), "n", ChatRoomType.OPEN, 1L);
            Long otherRoomId = chatRoomRepository.saveAndFlush(otherRoom).getId();

            Optional<Membership> found = membershipRepository
                    .findByUserIdAndRoomIdAndStatus(1L, otherRoomId, MembershipStatus.ACTIVE);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("uk_membership_active 부분 UNIQUE 제약")
    class UniqueConstraint {

        @Test
        @DisplayName("동일 (userId, roomId) ACTIVE 2건 시도 시 DataIntegrityViolationException")
        void uniqueConstraint_동일조합_ACTIVE_2건_예외() {
            saveActive(1L, roomId);

            assertThatThrownBy(() -> {
                membershipRepository.saveAndFlush(Membership.create(1L, roomId));
            }).isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("동일 (userId, roomId) LEFT는 여러 건 허용된다 (active_key가 'L:{id}'로 unique)")
        void uniqueConstraint_LEFT는_여러건허용() {
            saveLeft(1L, roomId);
            em.clear();

            saveLeft(1L, roomId);
            em.clear();

            List<Membership> all = membershipRepository.findAll();
            long leftCount = all.stream()
                    .filter(m -> m.getUserId().equals(1L)
                            && m.getRoomId().equals(roomId)
                            && m.getStatus() == MembershipStatus.LEFT)
                    .count();
            assertThat(leftCount).isEqualTo(2);
        }

        @Test
        @DisplayName("동일 (userId, roomId)에 ACTIVE 1건 + LEFT 1건 공존 허용")
        void uniqueConstraint_ACTIVE_LEFT_공존허용() {
            saveLeft(1L, roomId);
            em.clear();

            saveActive(1L, roomId);
            em.clear();

            List<Membership> all = membershipRepository.findAll();
            long activeCount = all.stream()
                    .filter(m -> m.getUserId().equals(1L) && m.getStatus() == MembershipStatus.ACTIVE)
                    .count();
            long leftCount = all.stream()
                    .filter(m -> m.getUserId().equals(1L) && m.getStatus() == MembershipStatus.LEFT)
                    .count();
            assertThat(activeCount).isEqualTo(1);
            assertThat(leftCount).isEqualTo(1);
        }
    }
}
