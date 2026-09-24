import java.util.HashMap;
import java.util.HashSet;

/** (Stretch) Counts suspicious activity per account and flags problematic customers (3+). */
public class AlertSystem 
{

    /**
     * indicates the number until a customer is problematic
     */
    public static final int THRESHOLD = 3;

    private final HashMap<String, Integer> suspiciousCounts = new HashMap<>();
    private final HashSet<String> flaggedAccounts = new HashSet<>();

    /** Adds one incident and flags the account once it reaches the threshold
     * @param accountNumber */
    public void recordSuspiciousActivity(String accountNumber) 
    {
        int count = suspiciousCounts.getOrDefault(accountNumber, 0) + 1;
        suspiciousCounts.put(accountNumber, count);
        if (count >= THRESHOLD) {
            flaggedAccounts.add(accountNumber);
        }
    }

    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * @param accountNumber
     * @return the flagged accounts
     */
    public boolean isProblematic(String accountNumber) 
    {
        return flaggedAccounts.contains(accountNumber);
    }

    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * @param accountNumber
     * @return the number of suspicious activity
     */
    public int getSuspiciousCount(String accountNumber) 
    {
        return suspiciousCounts.getOrDefault(accountNumber, 0);
    }
}
