package io.lbry.browser.exceptions;

public class LbryRequestException extends Exception {
    public LbryRequestException() {
        super();
    }
    public LbryRequestException(String message) {
        super(message);
    }
    public LbryRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
