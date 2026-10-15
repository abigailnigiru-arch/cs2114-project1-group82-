package cashclash;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// -------------------------------------------------------------------------
/**
 * Owns one account's balances, its daily-withdrawal tracking, and its
 * transaction history. All money is stored in cents.
 */
public class Account
{

    /** Name used for the checking balance. */
    public static final String CHECKING = "Checking";

    /** Name used for the savings balance. */
    public static final String SAVINGS = "Savings";

    /** Bank-wide daily withdrawal cap: $500.00. */
    public static final long DAILY_WITHDRAWAL_LIMIT_CENTS = 50_000;

    private final String accountNumber;
    private long checkingCents;
    private long savingsCents;
    private long dailyWithdrawalTotal;
    private LocalDate dailyWithdrawalDate;
    private final List<Transaction> transactionHistory = new ArrayList<>();


    // ----------------------------------------------------------
    /**
     * Creates an account; the initial balance starts in Checking.
     *
     * @param accountNumber
     *            the account number
     * @param initialBalanceCents
     *            starting balance in cents (cannot be negative)
     */
    public Account(String accountNumber, long initialBalanceCents)
    {
        if (initialBalanceCents < 0)
        {
            throw new IllegalArgumentException(
                "Initial balance cannot be negative.");
        }
        this.accountNumber = accountNumber;
        this.checkingCents = initialBalanceCents;
        this.savingsCents = 0;
        this.dailyWithdrawalTotal = 0;
        this.dailyWithdrawalDate = LocalDate.now();
    }


    // ----------------------------------------------------------
    /**
     * Gets the account number.
     *
     * @return the account number
     */
    public String getAccountNumber()
    {
        return accountNumber;
    }


    // ----------------------------------------------------------
    /**
     * Total of checking and savings, in cents.
     *
     * @return checking plus savings
     */
    public long getBalance()
    {
        return checkingCents + savingsCents;
    }


    // ----------------------------------------------------------
    /**
     * Balance of "Checking" or "Savings", in cents.
     *
     * @param accountType
     *            "Checking" or "Savings" (not case sensitive)
     * @return that balance in cents
     */
    public long getBalance(String accountType)
    {
        return normalizeType(accountType).equals(CHECKING)
            ? checkingCents
            : savingsCents;
    }


    // ----------------------------------------------------------
    /**
     * Gets the checking balance.
     *
     * @return checking balance in cents
     */
    public long getCheckingBalance()
    {
        return checkingCents;
    }


    // ----------------------------------------------------------
    /**
     * Gets the savings balance.
     *
     * @return savings balance in cents
     */
    public long getSavingsBalance()
    {
        return savingsCents;
    }


    // ----------------------------------------------------------
    /**
     * Deposits into Checking.
     *
     * @param amountCents
     *            amount in cents (must be greater than zero)
     */
    public void deposit(long amountCents)
    {
        deposit(amountCents, CHECKING);
    }


    // ----------------------------------------------------------
    /**
     * Deposits into the chosen balance and logs a Transaction.
     *
     * @param amountCents
     *            amount in cents (must be greater than zero)
     * @param accountType
     *            "Checking" or "Savings"
     */
    public void deposit(long amountCents, String accountType)
    {
        if (amountCents <= 0)
        {
            throw new IllegalArgumentException(
                "Deposit must be greater than zero.");
        }
        String type = normalizeType(accountType);
        if (type.equals(CHECKING))
        {
            checkingCents += amountCents;
        }
        else
        {
            savingsCents += amountCents;
        }
        transactionHistory.add(
            new Transaction(
                "Deposit",
                amountCents,
                type,
                "Deposit to " + type));
    }


    // ----------------------------------------------------------
    /**
     * Withdraws from Checking.
     *
     * @param amountCents
     *            amount in cents (must be greater than zero)
     * @throws InsufficientFundsException
     *             if Checking does not hold enough
     * @throws DailyLimitExceededException
     *             if this would pass the daily limit
     */
    public void withdraw(long amountCents)
        throws InsufficientFundsException,
        DailyLimitExceededException
    {
        withdraw(amountCents, CHECKING);
    }


    // ----------------------------------------------------------
    /**
     * Withdraws from the chosen balance if the balance and the daily limit
     * allow it, and logs a Transaction.
     *
     * @param amountCents
     *            amount in cents (must be greater than zero)
     * @param accountType
     *            "Checking" or "Savings"
     * @throws InsufficientFundsException
     *             if the chosen balance does not hold enough
     * @throws DailyLimitExceededException
     *             if this would pass the daily limit
     */
    public void withdraw(long amountCents, String accountType)
        throws InsufficientFundsException,
        DailyLimitExceededException
    {
        if (amountCents <= 0)
        {
            throw new IllegalArgumentException(
                "Withdrawal must be greater than zero.");
        }
        String type = normalizeType(accountType);
        resetDailyWithdrawalIfNewDay();

        long available = getBalance(type);
        if (amountCents > available)
        {
            throw new InsufficientFundsException(
                "Insufficient funds in " + type + ". Available: "
                    + Money.format(available));
        }
        if (dailyWithdrawalTotal + amountCents > DAILY_WITHDRAWAL_LIMIT_CENTS)
        {
            throw new DailyLimitExceededException(
                "Daily withdrawal limit of "
                    + Money.format(DAILY_WITHDRAWAL_LIMIT_CENTS)
                    + " exceeded. Remaining today: "
                    + Money.format(getRemainingDailyLimit()));
        }

        if (type.equals(CHECKING))
        {
            checkingCents -= amountCents;
        }
        else
        {
            savingsCents -= amountCents;
        }
        dailyWithdrawalTotal += amountCents;
        transactionHistory.add(
            new Transaction(
                "Withdrawal",
                amountCents,
                type,
                "Withdrawal from " + type));
    }


    // ----------------------------------------------------------
    /**
     * Transactions in the order they happened. Never null.
     *
     * @return a read-only copy of the history
     */
    public List<Transaction> getTransactionHistory()
    {
        return Collections
            .unmodifiableList(new ArrayList<>(transactionHistory));
    }


    // ----------------------------------------------------------
    /**
     * Zeroes today's running total if the calendar day has changed.
     */
    public void resetDailyWithdrawalIfNewDay()
    {
        LocalDate today = LocalDate.now();
        if (!today.equals(dailyWithdrawalDate))
        {
            dailyWithdrawalTotal = 0;
            dailyWithdrawalDate = today;
        }
    }


    // ----------------------------------------------------------
    /**
     * How much can still be withdrawn today.
     *
     * @return the daily limit minus today's withdrawals, in cents
     */
    public long getRemainingDailyLimit()
    {
        resetDailyWithdrawalIfNewDay();
        return DAILY_WITHDRAWAL_LIMIT_CENTS - dailyWithdrawalTotal;
    }


    // ----------------------------------------------------------
    /**
     * For unit tests only: pretend the last withdrawal happened on another
     * date.
     *
     * @param date
     *            the date to pretend it is
     */
    void setDailyWithdrawalDateForTesting(LocalDate date)
    {
        this.dailyWithdrawalDate = date;
    }


    private static String normalizeType(String accountType)
    {
        if (CHECKING.equalsIgnoreCase(accountType))
        {
            return CHECKING;
        }
        if (SAVINGS.equalsIgnoreCase(accountType))
        {
            return SAVINGS;
        }
        throw new IllegalArgumentException(
            "Account type must be Checking or Savings.");
    }
}
