package pl.norbit.playermarket.exception;

public class SQLQueryException extends RuntimeException{

    public SQLQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
