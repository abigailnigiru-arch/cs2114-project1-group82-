package cashclash;

// -------------------------------------------------------------------------
/**
 * DuplicateAccountException class
 * 
 *  @author abigailnigiru
 *  @version Sep 24, 2026
 */
public class DuplicateAccountException extends Exception
{
    // ----------------------------------------------------------
    /**
     * Create a new DuplicateAccountException object.
     * @param message
     */
    public DuplicateAccountException(String message)
    {
        super(message);
    }
}