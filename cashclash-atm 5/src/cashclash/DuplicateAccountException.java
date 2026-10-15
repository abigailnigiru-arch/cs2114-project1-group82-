package cashclash;

// -------------------------------------------------------------------------
/**
 * Thrown when creating an account whose number is already in use.
 *
 * @author abigailnigiru
 * @version Sep 24, 2026
 */
public class DuplicateAccountException
    extends Exception
{
    private static final long serialVersionUID = 1L;


    // ----------------------------------------------------------
    /**
     * Create a new DuplicateAccountException object.
     *
     * @param message
     *            explains what went wrong
     */
    public DuplicateAccountException(String message)
    {
        super(message);
    }
}
