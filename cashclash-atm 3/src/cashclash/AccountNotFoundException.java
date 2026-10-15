package cashclash;

// -------------------------------------------------------------------------
/**
 * Thrown when an account number does not match any account in the Bank.
 *
 * @author abigailnigiru
 * @version Sep 24, 2026
 */
public class AccountNotFoundException
    extends Exception
{
    private static final long serialVersionUID = 1L;


    // ----------------------------------------------------------
    /**
     * Create a new AccountNotFoundException object.
     *
     * @param message
     *            explains what went wrong
     */
    public AccountNotFoundException(String message)
    {
        super(message);
    }
}
