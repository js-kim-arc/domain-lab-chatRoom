package domainlab.chatroom.chat.application.service;

import domainlab.chatroom.chat.domain.model.JoinOutcome;
import domainlab.chatroom.chat.domain.model.Membership;
import domainlab.chatroom.chat.domain.model.MembershipResolver;
import domainlab.chatroom.chat.domain.model.MembershipStatus;
import domainlab.chatroom.chat.domain.repository.ChatRoomRepository;
import domainlab.chatroom.chat.domain.repository.MembershipRepository;
import domainlab.chatroom.chat.exception.ChatRoomDomainException;
import domainlab.chatroom.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MembershipCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipResolver membershipResolver;

    public MembershipCommandService(ChatRoomRepository chatRoomRepository,
                                    MembershipRepository membershipRepository,
                                    MembershipResolver membershipResolver) {
        this.chatRoomRepository = chatRoomRepository;
        this.membershipRepository = membershipRepository;
        this.membershipResolver = membershipResolver;
    }

    @Transactional
    public JoinChatRoomResult joinChatRoom(Long currentUserId, Long roomId) {
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> ChatRoomDomainException.of(ErrorCode.CR001));

        Optional<Membership> activeExisting = membershipRepository
                .findByUserIdAndRoomIdAndStatus(currentUserId, roomId, MembershipStatus.ACTIVE);

        JoinOutcome outcome = membershipResolver.resolveJoin(currentUserId, roomId, activeExisting);

        return switch (outcome) {
            case CREATE_NEW -> {
                Membership newMembership = Membership.create(currentUserId, roomId);
                membershipRepository.save(newMembership);
                yield new JoinChatRoomResult(newMembership, true);
            }
            case ALREADY_ACTIVE -> new JoinChatRoomResult(activeExisting.orElseThrow(), false);
            case REJOIN_EXISTING -> throw new IllegalStateException(
                    "REJOIN_EXISTING is not enabled in v1");
        };
    }
}
