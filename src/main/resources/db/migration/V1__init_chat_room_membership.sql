CREATE TABLE chat_room (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    topic       VARCHAR(100) NOT NULL,
    name        VARCHAR(30)  NOT NULL,
    type        VARCHAR(10)  NOT NULL,
    created_by  BIGINT       NOT NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT chk_chat_room_type CHECK (type IN ('OPEN', 'DM'))
);

CREATE TABLE membership (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    room_id     BIGINT       NOT NULL,
    status      VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    joined_at   DATETIME(6)  NOT NULL,
    left_at     DATETIME(6)  NULL,
    active_key  VARCHAR(50)  GENERATED ALWAYS AS (
                    CASE WHEN status = 'ACTIVE'
                         THEN CONCAT('A:', user_id, ':', room_id)
                         ELSE CONCAT('L:', id)
                    END
                ),

    PRIMARY KEY (id),
    CONSTRAINT uk_membership_active UNIQUE (active_key),
    CONSTRAINT fk_membership_room
        FOREIGN KEY (room_id) REFERENCES chat_room (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_membership_status
        CHECK (status IN ('ACTIVE', 'LEFT')),
    CONSTRAINT chk_membership_left_at_consistency
        CHECK (
            (status = 'ACTIVE' AND left_at IS NULL)
            OR (status = 'LEFT' AND left_at IS NOT NULL)
        )
);

CREATE INDEX idx_membership_user_room ON membership (user_id, room_id);
