package domainlab.chatroom.chat.domain.model;

import domainlab.chatroom.chat.exception.MembershipDomainException;
import domainlab.chatroom.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MembershipResolverTest {

    private final MembershipResolver resolver = new MembershipResolver();

    private static Membership active(Long userId, Long roomId) {
        return Membership.create(userId, roomId);
    }

    private static Membership left(Long userId, Long roomId) {
        Membership m = Membership.create(userId, roomId);
        m.leave();
        return m;
    }

    @Nested
    @DisplayName("resolveJoin")
    class ResolveJoin {

        @Test
        @DisplayName("기존 멤버십이 없으면 CREATE_NEW")
        void existing_empty_CREATE_NEW() {
            JoinOutcome outcome = resolver.resolveJoin(1L, 10L, Optional.empty());
            assertThat(outcome).isEqualTo(JoinOutcome.CREATE_NEW);
        }

        @Test
        @DisplayName("기존 멤버십이 ACTIVE면 ALREADY_ACTIVE (멱등)")
        void existing_ACTIVE_ALREADY_ACTIVE() {
            JoinOutcome outcome = resolver.resolveJoin(1L, 10L, Optional.of(active(1L, 10L)));
            assertThat(outcome).isEqualTo(JoinOutcome.ALREADY_ACTIVE);
        }

        @Test
        @DisplayName("기존 멤버십이 LEFT면 v1 정책에 따라 CREATE_NEW (REJOIN_EXISTING은 v2)")
        void existing_LEFT_v1정책_CREATE_NEW() {
            JoinOutcome outcome = resolver.resolveJoin(1L, 10L, Optional.of(left(1L, 10L)));
            assertThat(outcome).isEqualTo(JoinOutcome.CREATE_NEW);
        }

        @Test
        @DisplayName("userId가 null이면 C001")
        void userId_null_예외() {
            assertThatThrownBy(() -> resolver.resolveJoin(null, 10L, Optional.empty()))
                    .isInstanceOf(MembershipDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.C001);
        }

        @Test
        @DisplayName("roomId가 null이면 C001")
        void roomId_null_예외() {
            assertThatThrownBy(() -> resolver.resolveJoin(1L, null, Optional.empty()))
                    .isInstanceOf(MembershipDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.C001);
        }

        @Test
        @DisplayName("Optional 자체가 null이면 C001")
        void existing_null_예외() {
            assertThatThrownBy(() -> resolver.resolveJoin(1L, 10L, null))
                    .isInstanceOf(MembershipDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.C001);
        }
    }

    @Nested
    @DisplayName("resolveLeave")
    class ResolveLeave {

        @Test
        @DisplayName("기존 멤버십이 없으면 IGNORE (멱등)")
        void existing_empty_IGNORE() {
            LeaveOutcome outcome = resolver.resolveLeave(Optional.empty());
            assertThat(outcome).isEqualTo(LeaveOutcome.IGNORE);
        }

        @Test
        @DisplayName("기존 멤버십이 ACTIVE면 LEAVE")
        void existing_ACTIVE_LEAVE() {
            LeaveOutcome outcome = resolver.resolveLeave(Optional.of(active(1L, 10L)));
            assertThat(outcome).isEqualTo(LeaveOutcome.LEAVE);
        }

        @Test
        @DisplayName("기존 멤버십이 LEFT면 IGNORE (이미 이탈, 멱등)")
        void existing_LEFT_IGNORE() {
            LeaveOutcome outcome = resolver.resolveLeave(Optional.of(left(1L, 10L)));
            assertThat(outcome).isEqualTo(LeaveOutcome.IGNORE);
        }

        @Test
        @DisplayName("Optional 자체가 null이면 C001")
        void existing_null_예외() {
            assertThatThrownBy(() -> resolver.resolveLeave(null))
                    .isInstanceOf(MembershipDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.C001);
        }
    }

    @Nested
    @DisplayName("상태 변경 미수행 (책임 분리)")
    class NoStateChange {

        @Test
        @DisplayName("resolveJoin은 existing의 상태를 변경하지 않는다")
        void resolveJoin_상태변경하지않음() {
            Membership m = active(1L, 10L);
            MembershipStatus before = m.getStatus();

            resolver.resolveJoin(1L, 10L, Optional.of(m));

            assertThat(m.getStatus()).isEqualTo(before);
        }

        @Test
        @DisplayName("resolveLeave는 existing의 상태를 변경하지 않는다")
        void resolveLeave_상태변경하지않음() {
            Membership m = active(1L, 10L);
            MembershipStatus before = m.getStatus();

            resolver.resolveLeave(Optional.of(m));

            assertThat(m.getStatus()).isEqualTo(before);
        }
    }
}
