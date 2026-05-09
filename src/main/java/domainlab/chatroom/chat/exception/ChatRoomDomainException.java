package domainlab.chatroom.chat.exception;

import domainlab.chatroom.common.exception.DomainException;
import domainlab.chatroom.common.exception.ErrorCode;

public class ChatRoomDomainException extends DomainException {

    public ChatRoomDomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static ChatRoomDomainException of(ErrorCode errorCode) {
        return new ChatRoomDomainException(errorCode);
    }
}
