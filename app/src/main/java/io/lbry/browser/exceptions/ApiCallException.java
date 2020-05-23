package io.lbry.browser.exceptions;

public class ApiCallException extends Exception {
    public ApiCallException() {
        super();
    }
    public ApiCallException(String message) {
        super(message);
    }
    public ApiCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
