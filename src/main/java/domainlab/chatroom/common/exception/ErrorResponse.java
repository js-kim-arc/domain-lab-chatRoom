package domainlab.chatroom.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record ErrorResponse(
        String code,
        String message,
        String path,
        @JsonFormat(shape = JsonFormat.Shape.STRING) OffsetDateTime timestamp
) {

    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return new ErrorResponse(
                errorCode.code(),
                errorCode.message(),
                path,
                OffsetDateTime.now());
    }

    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(code, message, path, OffsetDateTime.now());
    }
}
