package domainlab.chatroom.chat.domain.repository;

import domainlab.chatroom.chat.domain.model.Membership;
import domainlab.chatroom.chat.domain.model.MembershipStatus;

import java.util.Optional;

public interface MembershipRepository {

    Membership save(Membership membership);

    Optional<Membership> findByUserIdAndRoomIdAndStatus(Long userId, Long roomId, MembershipStatus status);
}
