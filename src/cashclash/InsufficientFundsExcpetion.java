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
public class InsufficientFundsExcpetion extends Exception
{
    // ----------------------------------------------------------
    /**
     * Create a new InsufficientFundsExcpetion object.
     * @param message
     */
    public InsufficientFundsExcpetion(String message)
    {
        super(message);
    }
}