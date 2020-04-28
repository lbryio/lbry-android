package io.lbry.browser.exceptions;

public class WalletException extends Exception {
    public WalletException() {
        super();
    }
    public WalletException(String message) {
        super(message);
    }
    public WalletException(String message, Throwable cause) {
        super(message, cause);
    }
}
