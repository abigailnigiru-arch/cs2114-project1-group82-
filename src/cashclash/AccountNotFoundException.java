package cashclash;

// -------------------------------------------------------------------------
/**
 * AccountNotFoundException class
 * 
 *  @author abigailnigiru
 *  @version Sep 24, 2026
 */
public class AccountNotFoundException extends Exception
{
    // ----------------------------------------------------------
    /**
     * Create a new AccountNotFoundException object.
     * @param message
     */
    public AccountNotFoundException(String message)
    {
        super(message);
    }
}