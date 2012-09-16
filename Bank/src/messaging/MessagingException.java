package messaging;

import java.lang.Exception;

public class MessagingException extends Exception {

    public enum Type {
        INVALID_BRANCH_DECLARATION,
        INVALID_SRC_BRANCH,
        INVALID_DEST_BRANCH
    }

    public Type type;

    public MessagingException(Type t) {
        this.type = t;
    }
}


