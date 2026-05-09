package domainlab.chatroom.chat.infrastructure.persistence;

import domainlab.chatroom.chat.domain.model.Membership;
import domainlab.chatroom.chat.domain.model.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipJpaRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findByUserIdAndRoomIdAndStatus(Long userId, Long roomId, MembershipStatus status);
}
