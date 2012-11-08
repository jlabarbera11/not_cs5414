package messaging;

import java.lang.Exception;

public class MessagingException extends Exception {

    public enum Type {
        FILE_NOT_FOUND,
        UNRECOGNIZED_BRANCH,
        FAILED_SOCKET_CREATION,
        FAILED_REQUEST_SEND,
        FAILED_REQUEST_RECEIVE,
        FAILED_RESPONSE_SEND,
        FAILED_RESPONSE_RECEIVE,
        UNKNOWN_HOST,
        INVALID_TOPOLOGY,
        INVALID_BRANCH_DECLARATION,
        INVALID_SRC_BRANCH,
        INVALID_DEST_BRANCH,
        FAILED_SYNC_BUFFER,
        UNKNOWN_ERROR,
        FAILED_CONNECTION_TO_REPLICA,
        REPLICA_NOT_FOUND,
    }

    public Type type;

    public MessagingException(Type t) {
        this.type = t;
    }
}


