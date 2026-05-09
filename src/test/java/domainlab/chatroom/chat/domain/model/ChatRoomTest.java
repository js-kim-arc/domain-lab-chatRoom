package domainlab.chatroom.chat.domain.model;

import domainlab.chatroom.chat.exception.ChatRoomDomainException;
import domainlab.chatroom.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatRoomTest {

    private static final String VALID_NAME = "Spring 스터디";

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("정상 입력: ChatRoom 생성, createdAt이 자동 기록된다")
        void create_valid() {
            LocalDateTime before = LocalDateTime.now();
            ChatRoom room = ChatRoom.create(Topic.of("spring-boot"), VALID_NAME, ChatRoomType.OPEN, 1L);
            LocalDateTime after = LocalDateTime.now();

            assertThat(room.getTopic().value()).isEqualTo("spring-boot");
            assertThat(room.getName()).isEqualTo(VALID_NAME);
            assertThat(room.getType()).isEqualTo(ChatRoomType.OPEN);
            assertThat(room.getCreatedBy()).isEqualTo(1L);
            assertThat(room.getCreatedAt()).isBetween(before, after);
            assertThat(room.getUpdatedAt()).isBetween(before, after);
        }

        @Test
        @DisplayName("topic이 null이면 TP001")
        void create_topic_null_예외() {
            assertThatThrownBy(() -> ChatRoom.create(null, VALID_NAME, ChatRoomType.OPEN, 1L))
                    .isInstanceOf(ChatRoomDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.TP001);
        }

        @Test
        @DisplayName("name이 null이면 C001")
        void create_name_null_예외() {
            assertThatThrownBy(() -> ChatRoom.create(Topic.of("t"), null, ChatRoomType.OPEN, 1L))
                    .isInstanceOf(ChatRoomDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.C001);
        }

        @Test
        @DisplayName("name이 공백뿐이면 C001")
        void create_name_blank_예외() {
            assertThatThrownBy(() -> ChatRoom.create(Topic.of("t"), "   ", ChatRoomType.OPEN, 1L))
                    .isInstanceOf(ChatRoomDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.C001);
        }

        @Test
        @DisplayName("name이 31자면 CR002")
        void create_name_31자_예외() {
            String name = "a".repeat(31);
            assertThatThrownBy(() -> ChatRoom.create(Topic.of("t"), name, ChatRoomType.OPEN, 1L))
                    .isInstanceOf(ChatRoomDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.CR002);
        }

        @Test
        @DisplayName("name 30자 경계값은 허용된다")
        void create_name_30자_경계_valid() {
            String name = "a".repeat(30);
            ChatRoom room = ChatRoom.create(Topic.of("t"), name, ChatRoomType.OPEN, 1L);
            assertThat(room.getName()).isEqualTo(name);
        }

        @Test
        @DisplayName("name은 trim 처리되어 저장된다")
        void create_name_trim처리() {
            ChatRoom room = ChatRoom.create(Topic.of("t"), "  Spring  ", ChatRoomType.OPEN, 1L);
            assertThat(room.getName()).isEqualTo("Spring");
        }

        @Test
        @DisplayName("type이 null이면 C001")
        void create_type_null_예외() {
            assertThatThrownBy(() -> ChatRoom.create(Topic.of("t"), VALID_NAME, null, 1L))
                    .isInstanceOf(ChatRoomDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.C001);
        }

        @Test
        @DisplayName("type=DM은 v1에서 거부 — CR003")
        void create_type_DM_예외() {
            assertThatThrownBy(() -> ChatRoom.create(Topic.of("t"), VALID_NAME, ChatRoomType.DM, 1L))
                    .isInstanceOf(ChatRoomDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.CR003);
        }

        @Test
        @DisplayName("createdBy가 null이면 C001")
        void create_createdBy_null_예외() {
            assertThatThrownBy(() -> ChatRoom.create(Topic.of("t"), VALID_NAME, ChatRoomType.OPEN, null))
                    .isInstanceOf(ChatRoomDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.C001);
        }

        @Test
        @DisplayName("createdAt은 외부 주입 없이 now()로 자동 설정된다")
        void create_createdAt_자동기록() {
            LocalDateTime before = LocalDateTime.now();
            ChatRoom room = ChatRoom.create(Topic.of("t"), VALID_NAME, ChatRoomType.OPEN, 1L);
            LocalDateTime after = LocalDateTime.now();
            assertThat(room.getCreatedAt()).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("changeName")
    class ChangeName {

        @Test
        @DisplayName("정상 입력: name이 갱신된다")
        void changeName_valid() {
            ChatRoom room = ChatRoom.create(Topic.of("t"), VALID_NAME, ChatRoomType.OPEN, 1L);
            room.changeName("새 이름");
            assertThat(room.getName()).isEqualTo("새 이름");
        }

        @Test
        @DisplayName("null이면 C001")
        void changeName_null_예외() {
            ChatRoom room = ChatRoom.create(Topic.of("t"), VALID_NAME, ChatRoomType.OPEN, 1L);
            assertThatThrownBy(() -> room.changeName(null))
                    .isInstanceOf(ChatRoomDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.C001);
        }

        @Test
        @DisplayName("공백뿐이면 C001")
        void changeName_blank_예외() {
            ChatRoom room = ChatRoom.create(Topic.of("t"), VALID_NAME, ChatRoomType.OPEN, 1L);
            assertThatThrownBy(() -> room.changeName("   "))
                    .isInstanceOf(ChatRoomDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.C001);
        }

        @Test
        @DisplayName("31자면 CR002")
        void changeName_31자_예외() {
            ChatRoom room = ChatRoom.create(Topic.of("t"), VALID_NAME, ChatRoomType.OPEN, 1L);
            String name = "a".repeat(31);
            assertThatThrownBy(() -> room.changeName(name))
                    .isInstanceOf(ChatRoomDomainException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.CR002);
        }

        @Test
        @DisplayName("trim 후 저장된다")
        void changeName_trim처리() {
            ChatRoom room = ChatRoom.create(Topic.of("t"), VALID_NAME, ChatRoomType.OPEN, 1L);
            room.changeName("  새 이름  ");
            assertThat(room.getName()).isEqualTo("새 이름");
        }
    }

    @Nested
    @DisplayName("변경 불가 정책 (setter 부재)")
    class Immutability {

        @Test
        @DisplayName("topic은 setter 또는 changeTopic이 존재하지 않는다")
        void topic_setter_부재() {
            assertThat(hasMutator("Topic")).isFalse();
        }

        @Test
        @DisplayName("type은 setter 또는 changeType이 존재하지 않는다")
        void type_setter_부재() {
            assertThat(hasMutator("Type")).isFalse();
        }

        @Test
        @DisplayName("createdBy는 setter 또는 changeCreatedBy가 존재하지 않는다")
        void createdBy_setter_부재() {
            assertThat(hasMutator("CreatedBy")).isFalse();
        }

        private boolean hasMutator(String suffix) {
            return Arrays.stream(ChatRoom.class.getMethods())
                    .map(Method::getName)
                    .anyMatch(n -> n.equals("set" + suffix) || n.equals("change" + suffix));
        }
    }

    @Nested
    @DisplayName("isOpen")
    class IsOpen {

        @Test
        @DisplayName("type=OPEN이면 true")
        void isOpen_OPEN_true() {
            ChatRoom room = ChatRoom.create(Topic.of("t"), VALID_NAME, ChatRoomType.OPEN, 1L);
            assertThat(room.isOpen()).isTrue();
        }
    }
}
