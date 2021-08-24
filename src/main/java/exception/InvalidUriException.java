package exception;

public class InvalidUriException extends RuntimeException {

    public InvalidUriException() {
        super("유효하지 않은 Uri 입니다.");
    }
}
