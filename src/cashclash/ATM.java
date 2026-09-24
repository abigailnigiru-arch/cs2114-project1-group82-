package cashclash;

import java.util.List;

/**
 * Runs the session: login, routing user choices to the right Account method,
 * and logout. Owns no banking data or banking rules itself. The stretch
 * collaborators (SecurityManager, AlertSystem, CurrencyConverter) are optional:
 * pass null for any of them and that feature is simply off. Methods return the
 * text to show the user so the same ATM works with the GUI or a console menu.
 */
public class ATM
{

    private final Bank bank;
    private final SecurityManager securityManager;
    private final AlertSystem alertSystem;
    private final CurrencyConverter currencyConverter;

    private Account currentAccount;
    private boolean isSignedIn;

    public ATM(Bank bank)
    {
        this(bank, null, null, null);
    }

    // ----------------------------------------------------------
    /**
     * Create a new ATM object.
     * 
     * @param bank
     * @param alertSystem 
     * @param securityManager 
     */
    public ATM(Bank bank, AlertSystem alertSystem, SecurityManager securityManager)
    {
        this(bank, securityManager, alertSystem, null);
    }


    /** Creates an ATM with optional security and alert collaborators. */
    public ATM(Bank bank, SecurityManager securityManager, AlertSystem alertSystem)
    {
        this(bank, securityManager, alertSystem, null);
    }


    // ----------------------------------------------------------
    /**
     * Create a new ATM object.
     * 
     * @param bank
     * @param securityManager
     * @param alertSystem
     * @param currencyConverter
     */
    public ATM(
        Bank bank,
        SecurityManager securityManager,
        AlertSystem alertSystem,
        CurrencyConverter currencyConverter)
    {
        this.bank = bank;
        this.securityManager = securityManager;
        this.alertSystem = alertSystem;
        this.currencyConverter = currencyConverter;
        this.currentAccount = null;
        this.isSignedIn = false;
    }

    // ---------- feature flags (used by the GUI) ----------


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * 
     * @return securityManager
     */
    public boolean isSecurityEnabled()
    {
        return securityManager != null;
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * 
     * @return currencyConverter
     */
    public boolean isCurrencyEnabled()
    {
        return currencyConverter != null;
    }

    // ---------- login / logout ----------


    /**
     * True if this account has opted into double verification.
     * 
     * @param accountNumber
     * @return true
     */
    public boolean requiresPassword(String accountNumber)
    {
        return securityManager != null
            && securityManager.hasPassword(accountNumber);
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * 
     * @param accountNumber
     * @return trrue
     * @throws AccountNotFoundException
     */
    public boolean authenticate(String accountNumber)
        throws AccountNotFoundException
    {
        return authenticate(accountNumber, null);
    }


    /**
     * Starts a session. Returns false (and stays signed out) if the account has
     * a password and the given one is missing or wrong.
     * 
     * @param accountNumber
     * @param password
     * @return true
     * @throws AccountNotFoundException
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
     * Place a description of your method here.
     */
    public void logout()
    {
        currentAccount = null;
        isSignedIn = false;
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * 
     * @return isSignedIn
     */
    public boolean isSignedIn()
    {
        return isSignedIn;
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * 
     * @return currentAccount
     */
    public Account getCurrentAccount()
    {
        return currentAccount;
    }


    /**
     * Opens a new account; password is optional and only used if security is
     * enabled.
     * 
     * @param accountNumber
     * @param initialBalanceCents
     * @param password
     * @return account
     * @throws DuplicateAccountException
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


    /**
     * Turns raw text into cents; foreign currencies go to the converter when it
     * is enabled.
     * 
     * @param raw
     * @return parseAmount
     * @throws InvalidAmountException
     * @throws UnsupportedCurrencyException
     */
    public long parseAmount(String raw)
        throws InvalidAmountException,
        UnsupportedCurrencyException
    {
        return InputValidator.parseAmountCents(raw, currencyConverter);
    }

    // ---------- transactions ----------


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * 
     * @param amountCents
     * @param accountType
     * @return handleDeposit
     */
    public String handleDeposit(long amountCents, String accountType)
    {
        requireSignedIn();
        currentAccount.deposit(amountCents, accountType);
        return "Deposited " + Money.format(amountCents) + " to " + accountType
            + ". New " + accountType + " balance: "
            + Money.format(currentAccount.getBalance(accountType));
    }


    /**
     * Failed withdrawals are reported to the AlertSystem and then rethrown for
     * the caller to display.
     * 
     * @param amountCents
     * @param accountType
     * @return requiresSignIn
     * @throws InsufficientFundsException
     * @throws DailyLimitExceededException
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
     * Place a description of your method here.
     * 
     * @return showAccountSummary 
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
     * Place a description of your method here.
     * @return history 
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


    /** Delegates to Account so the ATM never keeps its own running total. 
     * @return remainingDailyLimit*/
    public long getRemainingDailyLimit()
    {
        requireSignedIn();
        return currentAccount.getRemainingDailyLimit();
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * @return true 
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
