package domainlab.chatroom.chat.domain.repository;

import domainlab.chatroom.chat.domain.model.Membership;

public interface MembershipRepository {

    Membership save(Membership membership);
}
