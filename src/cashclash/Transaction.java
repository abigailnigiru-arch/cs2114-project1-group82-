import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** An immutable record of one deposit or withdrawal. */
public final class Transaction
{

    private static final DateTimeFormatter FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String type;            // "Deposit" or "Withdrawal"
    private final long amountCents;
    private final String accountType;     // "Checking" or "Savings"
    private final String description;
    private final LocalDateTime timestamp;

    // ----------------------------------------------------------
    /**
     * Create a new Transaction object.
     * @param type
     * @param amountCents
     * @param accountType
     * @param description
     */
    public Transaction(
        String type,
        long amountCents,
        String accountType,
        String description)
    {
        if (amountCents <= 0)
        {
            throw new IllegalArgumentException(
                "Transaction amount must be greater than zero.");
        }
        if (type == null || accountType == null)
        {
            throw new IllegalArgumentException(
                "Type and account type are required.");
        }
        this.type = type;
        this.amountCents = amountCents;
        this.accountType = accountType;
        this.description = description == null ? "" : description;
        this.timestamp = LocalDateTime.now();
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * @return type
     */
    public String getType()
    {
        return type;
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * @return amountCents
     */
    public long getAmount()
    {
        return amountCents;
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * @return accountType
     */
    public String getAccountType()
    {
        return accountType;
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * @return description
     */
    public String getDescription()
    {
        return description;
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * @return timestamp
     */
    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }


    @Override
    public String toString()
    {
        return String.format(
            "%s | %-10s | %-8s | %12s | %s",
            timestamp.format(FORMAT),
            type,
            accountType,
            Money.format(amountCents),
            description);
    }
}
