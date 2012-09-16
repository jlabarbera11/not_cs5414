package messaging;

import java.lang.Exception;

public class MessagingException extends Exception {

    public enum Type {
        FAILED_SOCKET_CREATION,
        FAILED_REQUEST_SEND,
        FAILED_REQUEST_RECEIVE,
        FAILED_RESPONSE_SEND,
        FAILED_RESPONSE_RECEIVE,
        UNKNOWN_HOST,
        INVALID_BRANCH_DECLARATION,
        INVALID_SRC_BRANCH,
        INVALID_DEST_BRANCH,
        UNKNOWN_ERROR
    }

    public Type type;

    public MessagingException(Type t) {
        this.type = t;
    }
}


