package top.retain.nd.exception;

/**
 * @author Retain
 * @date 2021/10/21 10:27
 */
public class SmsCodeException extends Throwable {
    public SmsCodeException() {
    }

    public SmsCodeException(String message) {
        super(message);
    }

    public SmsCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmsCodeException(Throwable cause) {
        super(cause);
    }

    public SmsCodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
