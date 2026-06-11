package exceptions;

public class UnsafeMoveException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnsafeMoveException(String message) {
        super(message);
    }
	
	public UnsafeMoveException(String message, Throwable cause) {
		super(message, cause);
	}
}
