package domainlab.chatroom.chat.exception;

import domainlab.chatroom.common.exception.DomainException;
import domainlab.chatroom.common.exception.ErrorCode;

public class MembershipDomainException extends DomainException {

    public MembershipDomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static MembershipDomainException of(ErrorCode errorCode) {
        return new MembershipDomainException(errorCode);
    }
}
