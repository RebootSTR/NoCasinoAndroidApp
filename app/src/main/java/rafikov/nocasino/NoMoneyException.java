package rafikov.nocasino;

public class NoMoneyException extends RuntimeException {
    public NoMoneyException() {
    }

    public NoMoneyException(String message) {
        super(message);
    }

    public NoMoneyException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMoneyException(Throwable cause) {
        super(cause);
    }
}
