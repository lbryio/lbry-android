package io.lbry.browser.exceptions;

import lombok.Getter;

public class LbryioResponseException extends Exception {
    @Getter
    private int statusCode;
    public LbryioResponseException() {
        super();
    }
    public LbryioResponseException(String message) {
        super(message);
    }
    public LbryioResponseException(String message, Throwable cause) {
        super(message, cause);
    }
    public LbryioResponseException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    public LbryioResponseException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
