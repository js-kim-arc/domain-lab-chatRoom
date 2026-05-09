package domainlab.chatroom.chat.domain.model;

import domainlab.chatroom.chat.exception.MembershipDomainException;
import domainlab.chatroom.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MembershipResolver {

    public JoinOutcome resolveJoin(Long userId, Long roomId, Optional<Membership> existing) {
        if (userId == null || roomId == null || existing == null) {
            throw MembershipDomainException.of(ErrorCode.C001);
        }
        if (existing.isEmpty()) {
            return JoinOutcome.CREATE_NEW;
        }
        Membership m = existing.get();
        if (m.isActive()) {
            return JoinOutcome.ALREADY_ACTIVE;
        }
        return JoinOutcome.CREATE_NEW;
    }

    public LeaveOutcome resolveLeave(Optional<Membership> existing) {
        if (existing == null) {
            throw MembershipDomainException.of(ErrorCode.C001);
        }
        if (existing.isEmpty()) {
            return LeaveOutcome.IGNORE;
        }
        Membership m = existing.get();
        if (m.isActive()) {
            return LeaveOutcome.LEAVE;
        }
        return LeaveOutcome.IGNORE;
    }
}
