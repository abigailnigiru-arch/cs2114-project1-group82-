package cashclash;

// -------------------------------------------------------------------------
/**
 * Thrown by InputValidator when text is not an acceptable USD amount.
 *
 * @author abigailnigiru
 * @version Sep 24, 2026
 */
public class InvalidAmountException
    extends Exception
{
    private static final long serialVersionUID = 1L;


    // ----------------------------------------------------------
    /**
     * Create a new InvalidAmountException object.
     *
     * @param message
     *            explains what went wrong
     */
    public InvalidAmountException(String message)
    {
        super(message);
    }
}
