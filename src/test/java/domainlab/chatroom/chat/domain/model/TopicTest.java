package domainlab.chatroom.chat.domain.model;

import domainlab.chatroom.chat.exception.TopicDomainException;
import domainlab.chatroom.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopicTest {

    @Test
    @DisplayName("정상 입력은 그대로 저장된다")
    void of_정상입력_valid() {
        Topic topic = Topic.of("spring-boot");
        assertThat(topic.value()).isEqualTo("spring-boot");
    }

    @Test
    @DisplayName("양 끝 공백은 trim 된다")
    void of_trim처리() {
        Topic topic = Topic.of("  spring  ");
        assertThat(topic.value()).isEqualTo("spring");
    }

    @Test
    @DisplayName("대문자는 소문자로 변환된다")
    void of_소문자화() {
        Topic topic = Topic.of("Spring Boot");
        assertThat(topic.value()).isEqualTo("spring-boot");
    }

    @Test
    @DisplayName("허용 문자(영문/숫자/한글/하이픈) 외 문자는 제거된다")
    void of_허용문자필터링_특수문자제거() {
        Topic topic = Topic.of("spring@boot!");
        assertThat(topic.value()).isEqualTo("springboot");
    }

    @Test
    @DisplayName("한글은 허용 문자에 포함된다")
    void of_한글허용() {
        Topic topic = Topic.of("자바 스프링");
        assertThat(topic.value()).isEqualTo("자바-스프링");
    }

    @Test
    @DisplayName("공백은 하이픈으로 치환된다")
    void of_공백을_하이픈으로치환() {
        Topic topic = Topic.of("spring boot");
        assertThat(topic.value()).isEqualTo("spring-boot");
    }

    @Test
    @DisplayName("연속된 하이픈은 하나로 축약된다")
    void of_연속하이픈_축약() {
        Topic topic = Topic.of("spring---boot");
        assertThat(topic.value()).isEqualTo("spring-boot");
    }

    @Test
    @DisplayName("양 끝 하이픈은 제거된다")
    void of_양끝하이픈_제거() {
        Topic topic = Topic.of("-spring-");
        assertThat(topic.value()).isEqualTo("spring");
    }

    @Test
    @DisplayName("복합 정규화: trim → 소문자 → 필터 → 치환 → 축약 → 끝하이픈 제거")
    void of_복합정규화() {
        Topic topic = Topic.of("  Spring  Boot!! ");
        assertThat(topic.value()).isEqualTo("spring-boot");
    }

    @Test
    @DisplayName("rawValue가 null이면 TP001")
    void of_null_예외() {
        assertThatThrownBy(() -> Topic.of(null))
                .isInstanceOf(TopicDomainException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TP001);
    }

    @Test
    @DisplayName("정규화 후 결과가 빈 문자열이면 TP002")
    void of_정규화후_빈문자열_예외() {
        assertThatThrownBy(() -> Topic.of("!!!@@@"))
                .isInstanceOf(TopicDomainException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TP002);
    }

    @Test
    @DisplayName("양 끝 하이픈 제거 후 빈 문자열이 되어도 TP002")
    void of_정규화후_하이픈만남음_예외() {
        assertThatThrownBy(() -> Topic.of("---"))
                .isInstanceOf(TopicDomainException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TP002);
    }

    @Test
    @DisplayName("입력이 달라도 정규화 결과가 같으면 동등하다")
    void equals_같은정규화결과_동등() {
        Topic a = Topic.of("Spring Boot");
        Topic b = Topic.of("spring-boot");
        assertThat(a).isEqualTo(b);
    }

    @Test
    @DisplayName("동등한 Topic의 hashCode는 동일하다")
    void hashCode_같은정규화결과_동일() {
        Topic a = Topic.of("Spring Boot");
        Topic b = Topic.of("spring-boot");
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("Topic은 immutable — value에 대한 public setter가 존재하지 않는다")
    void of_immutable_value변경불가() {
        boolean hasValueMutator = Arrays.stream(Topic.class.getMethods())
                .map(Method::getName)
                .anyMatch(n -> n.equalsIgnoreCase("setValue") || n.equalsIgnoreCase("changeValue"));
        assertThat(hasValueMutator).isFalse();
    }
}
