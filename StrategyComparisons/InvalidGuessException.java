/**
 * Thrown when an invalid guess is set as the hidden guess
 */
public class InvalidGuessException extends RuntimeException {

    /**
     * Constructor without a message
     */
    public InvalidGuessException() {
        super();
    }

    /**
     * Constructor with a message
     * 
     * @param message The message for the exception
     */
    public InvalidGuessException(String message) {
        super(message);
    }
}
