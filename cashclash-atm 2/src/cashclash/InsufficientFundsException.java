package cashclash;

// -------------------------------------------------------------------------
/**
 * Thrown when a withdrawal is larger than the balance of the chosen account.
 *
 * @author abigailnigiru
 * @version Sep 24, 2026
 */
public class InsufficientFundsException
    extends Exception
{
    private static final long serialVersionUID = 1L;


    // ----------------------------------------------------------
    /**
     * Create a new InsufficientFundsException object.
     *
     * @param message
     *            explains what went wrong
     */
    public InsufficientFundsException(String message)
    {
        super(message);
    }
}
