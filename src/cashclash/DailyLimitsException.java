package cashclash;

// -------------------------------------------------------------------------
/**
 * Write a one-sentence summary of your class here. Follow it with additional
 * details about its purpose, what abstraction it represents, and how to use it.
 * 
 * @author abigailnigiru
 * @version Sep 24, 2026
 */
public class DailyLimitsException
    extends Exception
{
    // ----------------------------------------------------------
    /**
     * Create a new DailyLimitExceededException object.
     * 
     * @param message
     */
    public DailyLimitsException(String message)
    {
        super(message);
    }
}
