package cashclash;

// -------------------------------------------------------------------------
/**
 * Thrown when a withdrawal would push today's total over the daily limit.
 *
 * @author abigailnigiru
 * @version Sep 24, 2026
 */
public class DailyLimitExceededException
    extends Exception
{
    private static final long serialVersionUID = 1L;


    // ----------------------------------------------------------
    /**
     * Create a new DailyLimitExceededException object.
     *
     * @param message
     *            explains what went wrong
     */
    public DailyLimitExceededException(String message)
    {
        super(message);
    }
}
