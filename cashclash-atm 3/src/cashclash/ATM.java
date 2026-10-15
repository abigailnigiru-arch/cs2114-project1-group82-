package cashclash;

import java.util.List;

// -------------------------------------------------------------------------
/**
 * Runs the session: login, routing user choices to the right Account method,
 * and logout. Owns no banking data or banking rules itself.
 *
 * The stretch collaborators (SecurityManager and AlertSystem) are optional:
 * pass null for either one and that feature is simply off. Methods return the
 * text to show the user so the same ATM works with the GUI or a console menu.
 */
public class ATM
{

    private final Bank bank;
    private final SecurityManager securityManager;
    private final AlertSystem alertSystem;

    private Account currentAccount;
    private boolean isSignedIn;


    // ----------------------------------------------------------
    /**
     * Create a new ATM with no stretch features.
     *
     * @param bank
     *            the bank that owns all the accounts
     */
    public ATM(Bank bank)
    {
        this(bank, null, null);
    }


    // ----------------------------------------------------------
    /**
     * Create a new ATM object.
     *
     * @param bank
     *            the bank that owns all the accounts
     * @param securityManager
     *            checks passwords, or null to turn double verification off
     * @param alertSystem
     *            counts suspicious activity, or null to turn alerts off
     */
    public ATM(
        Bank bank,
        SecurityManager securityManager,
        AlertSystem alertSystem)
    {
        this.bank = bank;
        this.securityManager = securityManager;
        this.alertSystem = alertSystem;
        this.currentAccount = null;
        this.isSignedIn = false;
    }

    // ---------- feature flags (used by the GUI) ----------


    // ----------------------------------------------------------
    /**
     * Tells the GUI whether to show a password box.
     *
     * @return true if a SecurityManager was provided
     */
    public boolean isSecurityEnabled()
    {
        return securityManager != null;
    }

    // ---------- login / logout ----------


    // ----------------------------------------------------------
    /**
     * True if this account has opted into double verification.
     *
     * @param accountNumber
     *            the account to check
     * @return true if the account has a password
     */
    public boolean requiresPassword(String accountNumber)
    {
        return securityManager != null
            && securityManager.hasPassword(accountNumber);
    }


    // ----------------------------------------------------------
    /**
     * Starts a session without a password.
     *
     * @param accountNumber
     *            the account to log into
     * @return true if the session started
     * @throws AccountNotFoundException
     *             if the number is malformed or no such account exists
     */
    public boolean authenticate(String accountNumber)
        throws AccountNotFoundException
    {
        return authenticate(accountNumber, null);
    }


    // ----------------------------------------------------------
    /**
     * Starts a session. Returns false (and stays signed out) if the account has
     * a password and the given one is missing or wrong.
     *
     * @param accountNumber
     *            the account to log into
     * @param password
     *            the password typed in, or null if none was typed
     * @return true if the session started
     * @throws AccountNotFoundException
     *             if the number is malformed or no such account exists
     */
    public boolean authenticate(String accountNumber, String password)
        throws AccountNotFoundException
    {
        logout();
        if (!InputValidator.isValidAccountNumber(accountNumber))
        {
            throw new AccountNotFoundException(
                "'" + accountNumber + "' is not a valid account number.");
        }
        Account account = bank.getAccount(accountNumber);
        if (requiresPassword(accountNumber) && (password == null
            || !securityManager.verifyPassword(accountNumber, password)))
        {
            return false;
        }
        currentAccount = account;
        isSignedIn = true;
        return true;
    }


    // ----------------------------------------------------------
    /**
     * Ends the current session. Does nothing if nobody is signed in.
     */
    public void logout()
    {
        currentAccount = null;
        isSignedIn = false;
    }


    // ----------------------------------------------------------
    /**
     * Whether someone is logged in right now.
     *
     * @return true if a session is active
     */
    public boolean isSignedIn()
    {
        return isSignedIn;
    }


    // ----------------------------------------------------------
    /**
     * The account for the current session.
     *
     * @return the current account, or null if nobody is signed in
     */
    public Account getCurrentAccount()
    {
        return currentAccount;
    }


    // ----------------------------------------------------------
    /**
     * Opens a new account; the password is optional and only used if security
     * is enabled.
     *
     * @param accountNumber
     *            a new 5-digit account number
     * @param initialBalanceCents
     *            starting balance in cents
     * @param password
     *            optional password (null or empty for none)
     * @return the new account
     * @throws DuplicateAccountException
     *             if the account number is already taken
     */
    public Account createAccount(
        String accountNumber,
        long initialBalanceCents,
        String password)
        throws DuplicateAccountException
    {
        if (!InputValidator.isValidAccountNumber(accountNumber))
        {
            throw new IllegalArgumentException(
                "Account numbers must be exactly 5 digits.");
        }
        Account account =
            bank.createAccount(accountNumber, initialBalanceCents);
        if (securityManager != null && password != null && !password.isEmpty())
        {
            securityManager.setPassword(accountNumber, password);
        }
        return account;
    }

    // ---------- input ----------


    // ----------------------------------------------------------
    /**
     * Turns raw text into cents (USD only).
     *
     * @param raw
     *            what the user typed
     * @return the amount in cents
     * @throws InvalidAmountException
     *             if the text is not a valid positive USD amount
     */
    public long parseAmount(String raw)
        throws InvalidAmountException
    {
        return InputValidator.parseAmountCents(raw);
    }

    // ---------- transactions ----------


    // ----------------------------------------------------------
    /**
     * Deposits into the current account.
     *
     * @param amountCents
     *            amount in cents
     * @param accountType
     *            "Checking" or "Savings"
     * @return a message to show the user
     */
    public String handleDeposit(long amountCents, String accountType)
    {
        requireSignedIn();
        currentAccount.deposit(amountCents, accountType);
        return "Deposited " + Money.format(amountCents) + " to " + accountType
            + ". New " + accountType + " balance: "
            + Money.format(currentAccount.getBalance(accountType));
    }


    // ----------------------------------------------------------
    /**
     * Withdraws from the current account. Failed withdrawals are reported to
     * the AlertSystem and then rethrown for the caller to display.
     *
     * @param amountCents
     *            amount in cents
     * @param accountType
     *            "Checking" or "Savings"
     * @return a message to show the user
     * @throws InsufficientFundsException
     *             if the chosen account does not hold enough
     * @throws DailyLimitExceededException
     *             if the withdrawal would pass the daily limit
     */
    public String handleWithdrawal(long amountCents, String accountType)
        throws InsufficientFundsException,
        DailyLimitExceededException
    {
        requireSignedIn();
        try
        {
            currentAccount.withdraw(amountCents, accountType);
        }
        catch (InsufficientFundsException | DailyLimitExceededException e)
        {
            if (alertSystem != null)
            {
                alertSystem.recordSuspiciousActivity(
                    currentAccount.getAccountNumber());
            }
            throw e;
        }
        return "Withdrew " + Money.format(amountCents) + " from " + accountType
            + ". New " + accountType + " balance: "
            + Money.format(currentAccount.getBalance(accountType));
    }

    // ---------- display ----------


    // ----------------------------------------------------------
    /**
     * Shows both balances and how much can still be withdrawn today.
     *
     * @return the summary text
     */
    public String showAccountSummary()
    {
        requireSignedIn();
        return "Checking: " + Money.format(currentAccount.getCheckingBalance())
            + "\n" + "Savings:  "
            + Money.format(currentAccount.getSavingsBalance()) + "\n"
            + "Left to withdraw today: "
            + Money.format(getRemainingDailyLimit());
    }


    // ----------------------------------------------------------
    /**
     * Shows the transaction history in the order it happened.
     *
     * @return the history text, or a "no transactions" message
     */
    public String showHistory()
    {
        requireSignedIn();
        List<Transaction> history = currentAccount.getTransactionHistory();
        if (history.isEmpty())
        {
            return "No transactions yet.";
        }
        StringBuilder sb = new StringBuilder();
        for (Transaction t : history)
        {
            sb.append(t).append("\n");
        }
        return sb.toString();
    }


    // ----------------------------------------------------------
    /**
     * Delegates to Account so the ATM never keeps its own running total.
     *
     * @return cents still available to withdraw today
     */
    public long getRemainingDailyLimit()
    {
        requireSignedIn();
        return currentAccount.getRemainingDailyLimit();
    }


    // ----------------------------------------------------------
    /**
     * Whether the AlertSystem has flagged the signed-in account.
     *
     * @return true if the current account is flagged as problematic
     */
    public boolean isCurrentAccountFlagged()
    {
        return alertSystem != null && isSignedIn
            && alertSystem.isProblematic(currentAccount.getAccountNumber());
    }


    private void requireSignedIn()
    {
        if (!isSignedIn || currentAccount == null)
        {
            throw new IllegalStateException("Please log in first.");
        }
    }
}
