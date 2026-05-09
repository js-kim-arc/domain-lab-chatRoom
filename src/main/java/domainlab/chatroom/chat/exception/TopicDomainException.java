package domainlab.chatroom.chat.exception;

import domainlab.chatroom.common.exception.DomainException;
import domainlab.chatroom.common.exception.ErrorCode;

public class TopicDomainException extends DomainException {

    public TopicDomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static TopicDomainException of(ErrorCode errorCode) {
        return new TopicDomainException(errorCode);
    }
}
