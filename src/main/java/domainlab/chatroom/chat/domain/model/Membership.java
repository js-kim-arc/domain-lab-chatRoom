package domainlab.chatroom.chat.domain.model;

import domainlab.chatroom.chat.exception.MembershipDomainException;
import domainlab.chatroom.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "membership")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private MembershipStatus status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    protected Membership() {
    }

    private Membership(Long userId, Long roomId, LocalDateTime now) {
        this.userId = userId;
        this.roomId = roomId;
        this.status = MembershipStatus.ACTIVE;
        this.joinedAt = now;
        this.leftAt = null;
    }

    public static Membership create(Long userId, Long roomId) {
        if (userId == null) {
            throw MembershipDomainException.of(ErrorCode.C001);
        }
        if (roomId == null) {
            throw MembershipDomainException.of(ErrorCode.C001);
        }
        return new Membership(userId, roomId, LocalDateTime.now());
    }

    public void leave() {
        if (status == MembershipStatus.LEFT) {
            return;
        }
        this.status = MembershipStatus.LEFT;
        this.leftAt = LocalDateTime.now();
    }

    public void rejoin() {
        if (status == MembershipStatus.ACTIVE) {
            return;
        }
        this.status = MembershipStatus.ACTIVE;
        this.joinedAt = LocalDateTime.now();
        this.leftAt = null;
    }

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE;
    }

    public boolean hasLeft() {
        return status == MembershipStatus.LEFT;
    }

    public boolean belongsTo(Long userId, Long roomId) {
        if (userId == null || roomId == null) {
            return false;
        }
        return Objects.equals(this.userId, userId) && Objects.equals(this.roomId, roomId);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public LocalDateTime getLeftAt() {
        return leftAt;
    }
}
