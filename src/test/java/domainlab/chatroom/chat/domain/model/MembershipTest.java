package domainlab.chatroom.chat.domain.model;

import domainlab.chatroom.chat.exception.MembershipDomainException;
import domainlab.chatroom.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MembershipTest {

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("정상 생성: status=ACTIVE, joinedAt=now, leftAt=null")
        void create_valid() {
            LocalDateTime before = LocalDateTime.now();
            Membership m = Membership.create(1L, 10L);
            LocalDateTime after = LocalDateTime.now();

            assertThat(m.getUserId()).isEqualTo(1L);
            assertThat(m.getRoomId()).isEqualTo(10L);
            assertThat(m.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
            assertThat(m.getJoinedAt()).isBetween(before, after);
            assertThat(m.getLeftAt()).isNull();
        }

        @Test
        @DisplayName("userId가 null이면 C001")
        void create_userId_null_예외() {
            assertThatThrownBy(() -> Membership.create(null, 10L))
                    .isInstanceOf(MembershipDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.C001);
        }

        @Test
        @DisplayName("roomId가 null이면 C001")
        void create_roomId_null_예외() {
            assertThatThrownBy(() -> Membership.create(1L, null))
                    .isInstanceOf(MembershipDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.C001);
        }

        @Test
        @DisplayName("생성 시 status는 반드시 ACTIVE로 초기화된다")
        void create_status_ACTIVE초기화() {
            Membership m = Membership.create(1L, 10L);
            assertThat(m.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
            assertThat(m.isActive()).isTrue();
        }

        @Test
        @DisplayName("joinedAt은 외부 주입 없이 now()로 자동 설정된다")
        void create_joinedAt_자동기록() {
            LocalDateTime before = LocalDateTime.now();
            Membership m = Membership.create(1L, 10L);
            LocalDateTime after = LocalDateTime.now();
            assertThat(m.getJoinedAt()).isBetween(before, after);
        }

        @Test
        @DisplayName("신규 멤버십은 LEFT 구간이 없으므로 leftAt=null")
        void create_leftAt_null초기화() {
            Membership m = Membership.create(1L, 10L);
            assertThat(m.getLeftAt()).isNull();
            assertThat(m.hasLeft()).isFalse();
        }
    }
}
