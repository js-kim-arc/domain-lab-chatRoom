package domainlab.chatroom.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    CR001("CR001", "채팅방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CR002("CR002", "채팅방 이름은 1자 이상 30자 이하여야 합니다.", HttpStatus.BAD_REQUEST),
    CR003("CR003", "v1에서는 OPEN 타입의 채팅방만 생성할 수 있습니다.", HttpStatus.BAD_REQUEST),

    TP001("TP001", "토픽은 비어 있을 수 없습니다.", HttpStatus.BAD_REQUEST),
    TP002("TP002", "정규화 후 토픽이 빈 문자열이 됩니다.", HttpStatus.BAD_REQUEST),

    MB001("MB001", "멤버십을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MB002("MB002", "해당 채팅방의 활성 멤버가 아닙니다.", HttpStatus.FORBIDDEN),

    C001("C001", "필수 값이 비어 있거나 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
