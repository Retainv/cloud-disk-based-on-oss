package top.retain.nd.exception;

/**
 * @author Retain
 * @date 2021/10/3 16:12
 */
public class UserNotLoginException extends RuntimeException{
    public UserNotLoginException() {
    }

    public UserNotLoginException(String message) {
        super(message);
    }

    public UserNotLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotLoginException(Throwable cause) {
        super(cause);
    }

    public UserNotLoginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
