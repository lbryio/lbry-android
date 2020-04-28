package io.lbry.browser.exceptions;

public class LbryResponseException extends Exception {
    public LbryResponseException() {
        super();
    }
    public LbryResponseException(String message) {
        super(message);
    }
    public LbryResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
