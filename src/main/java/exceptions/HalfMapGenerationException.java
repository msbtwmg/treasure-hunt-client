package exceptions;

public class HalfMapGenerationException extends Exception {

	private static final long serialVersionUID = 1L;

	public HalfMapGenerationException(String message) {
        super(message);
    }
	
	public HalfMapGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
}
