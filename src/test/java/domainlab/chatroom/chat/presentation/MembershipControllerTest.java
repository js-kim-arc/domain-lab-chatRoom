package domainlab.chatroom.chat.presentation;

import domainlab.chatroom.chat.application.service.JoinChatRoomResult;
import domainlab.chatroom.chat.application.service.MembershipCommandService;
import domainlab.chatroom.chat.domain.model.Membership;
import domainlab.chatroom.chat.exception.ChatRoomDomainException;
import domainlab.chatroom.common.auth.CurrentUserIdArgumentResolver;
import domainlab.chatroom.common.config.SecurityConfig;
import domainlab.chatroom.common.config.WebMvcConfig;
import domainlab.chatroom.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MembershipController.class)
@Import({SecurityConfig.class, WebMvcConfig.class, CurrentUserIdArgumentResolver.class})
class MembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MembershipCommandService membershipCommandService;

    private static Membership membershipWithId(Long id, Long userId, Long roomId) {
        Membership m = Membership.create(userId, roomId);
        ReflectionTestUtils.setField(m, "id", id);
        return m;
    }

    @Test
    @DisplayName("정상 입장은 201 Created로 응답한다")
    void joinRoom_201() throws Exception {
        Membership saved = membershipWithId(101L, 42L, 1L);
        given(membershipCommandService.joinChatRoom(42L, 1L))
                .willReturn(new JoinChatRoomResult(saved, true));

        mockMvc.perform(post("/api/v1/chat-rooms/{roomId}/memberships", 1L)
                        .with(oauth2Login().attributes(attrs -> attrs.put("id", 42L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membershipId").value(101))
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.roomId").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.leftAt").doesNotExist());
    }

    @Test
    @DisplayName("이미 ACTIVE인 경우 200 OK로 기존 멤버십을 멱등 반환한다")
    void joinRoom_already_active_200() throws Exception {
        Membership existing = membershipWithId(88L, 42L, 1L);
        // joinedAt을 과거로 설정해 \"기존 멤버십이 그대로 반환되는지\" 확인
        ReflectionTestUtils.setField(existing, "joinedAt", LocalDateTime.of(2026, 3, 20, 15, 30));
        given(membershipCommandService.joinChatRoom(42L, 1L))
                .willReturn(new JoinChatRoomResult(existing, false));

        mockMvc.perform(post("/api/v1/chat-rooms/{roomId}/memberships", 1L)
                        .with(oauth2Login().attributes(attrs -> attrs.put("id", 42L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membershipId").value(88))
                .andExpect(jsonPath("$.joinedAt").value("2026-03-20T15:30:00"));
    }

    @Test
    @DisplayName("존재하지 않는 방 ID는 CR001 → HTTP 404")
    void joinRoom_room_미존재_404() throws Exception {
        given(membershipCommandService.joinChatRoom(anyLong(), anyLong()))
                .willThrow(ChatRoomDomainException.of(ErrorCode.CR001));

        mockMvc.perform(post("/api/v1/chat-rooms/{roomId}/memberships", 999L)
                        .with(oauth2Login().attributes(attrs -> attrs.put("id", 42L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CR001"));
    }

    @Test
    @DisplayName("인증된 사용자의 id가 currentUserId로 Service에 전달된다")
    void joinRoom_currentUserId_전달() throws Exception {
        Membership saved = membershipWithId(101L, 99L, 10L);
        given(membershipCommandService.joinChatRoom(eq(99L), eq(10L)))
                .willReturn(new JoinChatRoomResult(saved, true));

        mockMvc.perform(post("/api/v1/chat-rooms/{roomId}/memberships", 10L)
                        .with(oauth2Login().attributes(attrs -> attrs.put("id", 99L))))
                .andExpect(status().isCreated());

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> roomIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(membershipCommandService).joinChatRoom(userIdCaptor.capture(), roomIdCaptor.capture());
        assertThat(userIdCaptor.getValue()).isEqualTo(99L);
        assertThat(roomIdCaptor.getValue()).isEqualTo(10L);
    }

    @Test
    @DisplayName("PathVariable {roomId}가 Long으로 바인딩된다")
    void joinRoom_pathVariable_바인딩() throws Exception {
        Membership saved = membershipWithId(101L, 42L, 12345L);
        given(membershipCommandService.joinChatRoom(eq(42L), eq(12345L)))
                .willReturn(new JoinChatRoomResult(saved, true));

        mockMvc.perform(post("/api/v1/chat-rooms/{roomId}/memberships", 12345L)
                        .with(oauth2Login().attributes(attrs -> attrs.put("id", 42L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value(12345));
    }
}
