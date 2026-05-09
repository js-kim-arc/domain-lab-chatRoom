package domainlab.chatroom.chat.infrastructure.persistence;

import domainlab.chatroom.chat.domain.model.Membership;
import domainlab.chatroom.chat.domain.model.MembershipStatus;
import domainlab.chatroom.chat.domain.repository.MembershipRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MembershipRepositoryAdapter implements MembershipRepository {

    private final MembershipJpaRepository jpa;

    public MembershipRepositoryAdapter(MembershipJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Membership save(Membership membership) {
        return jpa.save(membership);
    }

    @Override
    public Optional<Membership> findByUserIdAndRoomIdAndStatus(Long userId, Long roomId, MembershipStatus status) {
        return jpa.findByUserIdAndRoomIdAndStatus(userId, roomId, status);
    }
}
