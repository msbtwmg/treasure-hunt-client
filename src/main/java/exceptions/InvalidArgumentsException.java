package exceptions;

public class InvalidArgumentsException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidArgumentsException(String message) {
        super(message);
    }
}
