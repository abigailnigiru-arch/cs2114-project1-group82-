package cashclash;

// -------------------------------------------------------------------------
/**
 *  Write a one-sentence summary of your class here.
 *  Follow it with additional details about its purpose, what abstraction
 *  it represents, and how to use it.
 * 
 *  @author abigailnigiru
 *  @version Sep 24, 2026
 */
public class InvalidAmountException extends Exception {

    // ----------------------------------------------------------
    /**
     * Create a new InvalidAmountException object.
     * @param message
     */
    public InvalidAmountException(String message) {
        super(message);
    }
}
