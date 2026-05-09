package domainlab.chatroom.chat.infrastructure.persistence;

import domainlab.chatroom.chat.domain.model.Membership;
import domainlab.chatroom.chat.domain.repository.MembershipRepository;
import org.springframework.stereotype.Repository;

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
}
