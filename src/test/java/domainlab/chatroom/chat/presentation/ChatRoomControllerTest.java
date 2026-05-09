package domainlab.chatroom.chat.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import domainlab.chatroom.chat.application.service.ChatRoomCommandService;
import domainlab.chatroom.chat.domain.model.ChatRoom;
import domainlab.chatroom.chat.domain.model.ChatRoomType;
import domainlab.chatroom.chat.domain.model.Topic;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatRoomController.class)
@Import({SecurityConfig.class, WebMvcConfig.class, CurrentUserIdArgumentResolver.class})
class ChatRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ChatRoomCommandService chatRoomCommandService;

    private static ChatRoom roomWithId(Long id, String topicValue, String name,
                                       ChatRoomType type, Long createdBy) {
        ChatRoom room = ChatRoom.create(Topic.of(topicValue), name, type, createdBy);
        ReflectionTestUtils.setField(room, "id", id);
        return room;
    }

    @Test
    @DisplayName("정상 요청은 201 Created로 응답한다")
    void createRoom_valid_201() throws Exception {
        ChatRoom saved = roomWithId(1L, "Spring Boot", "스프링 스터디", ChatRoomType.OPEN, 42L);
        given(chatRoomCommandService.createChatRoom(anyString(), anyString(), anyString(), eq(42L)))
                .willReturn(saved);

        String body = objectMapper.writeValueAsString(Map.of(
                "topic", "Spring Boot",
                "name", "스프링 스터디",
                "type", "OPEN"
        ));

        mockMvc.perform(post("/api/v1/chat-rooms")
                        .with(oauth2Login().attributes(attrs -> attrs.put("id", 42L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value(1))
                .andExpect(jsonPath("$.topic").value("spring-boot"))
                .andExpect(jsonPath("$.name").value("스프링 스터디"))
                .andExpect(jsonPath("$.type").value("OPEN"))
                .andExpect(jsonPath("$.createdBy").value(42))
                .andExpect(jsonPath("$.memberCount").value(1));
    }

    @Test
    @DisplayName("topic이 blank면 도메인이 TP001로 거부 → HTTP 400")
    void createRoom_topic_blank_400() throws Exception {
        given(chatRoomCommandService.createChatRoom(anyString(), anyString(), anyString(), anyLong()))
                .willThrow(ChatRoomDomainException.of(ErrorCode.TP001));

        String body = objectMapper.writeValueAsString(Map.of(
                "topic", "",
                "name", "name",
                "type", "OPEN"
        ));

        mockMvc.perform(post("/api/v1/chat-rooms")
                        .with(oauth2Login().attributes(attrs -> attrs.put("id", 42L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TP001"));
    }

    @Test
    @DisplayName("name이 blank면 @NotBlank 위반 → HTTP 400 + C001")
    void createRoom_name_blank_400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "topic", "topic",
                "name", "",
                "type", "OPEN"
        ));

        mockMvc.perform(post("/api/v1/chat-rooms")
                        .with(oauth2Login().attributes(attrs -> attrs.put("id", 42L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("name이 31자면 도메인이 CR002로 거부 → HTTP 400")
    void createRoom_name_31자_400() throws Exception {
        given(chatRoomCommandService.createChatRoom(anyString(), anyString(), anyString(), anyLong()))
                .willThrow(ChatRoomDomainException.of(ErrorCode.CR002));

        String body = objectMapper.writeValueAsString(Map.of(
                "topic", "topic",
                "name", "a".repeat(31),
                "type", "OPEN"
        ));

        mockMvc.perform(post("/api/v1/chat-rooms")
                        .with(oauth2Login().attributes(attrs -> attrs.put("id", 42L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CR002"));
    }

    @Test
    @DisplayName("type 필드가 누락되면 @NotBlank 위반 → HTTP 400 + C001")
    void createRoom_type_누락_400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "topic", "topic",
                "name", "name"
                // type 필드 누락
        ));

        mockMvc.perform(post("/api/v1/chat-rooms")
                        .with(oauth2Login().attributes(attrs -> attrs.put("id", 42L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"));
    }

    @Test
    @DisplayName("type=DM은 Controller를 통과해 Service에서 CR003으로 거부")
    void createRoom_type_DM_요청_거부() throws Exception {
        given(chatRoomCommandService.createChatRoom(anyString(), anyString(), eq("DM"), anyLong()))
                .willThrow(ChatRoomDomainException.of(ErrorCode.CR003));

        String body = objectMapper.writeValueAsString(Map.of(
                "topic", "topic",
                "name", "name",
                "type", "DM"
        ));

        mockMvc.perform(post("/api/v1/chat-rooms")
                        .with(oauth2Login().attributes(attrs -> attrs.put("id", 42L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CR003"));
    }

    @Test
    @DisplayName("인증된 사용자의 id가 currentUserId로 Service에 전달된다")
    void createRoom_currentUserId_전달() throws Exception {
        ChatRoom saved = roomWithId(1L, "topic", "name", ChatRoomType.OPEN, 99L);
        given(chatRoomCommandService.createChatRoom(anyString(), anyString(), anyString(), eq(99L)))
                .willReturn(saved);

        String body = objectMapper.writeValueAsString(Map.of(
                "topic", "topic",
                "name", "name",
                "type", "OPEN"
        ));

        mockMvc.perform(post("/api/v1/chat-rooms")
                        .with(oauth2Login().attributes(attrs -> attrs.put("id", 99L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(chatRoomCommandService).createChatRoom(anyString(), anyString(), anyString(), userIdCaptor.capture());
        assertThat(userIdCaptor.getValue()).isEqualTo(99L);
    }
}
