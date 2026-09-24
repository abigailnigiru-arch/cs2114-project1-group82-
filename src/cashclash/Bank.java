package cashclash;

import java.util.HashMap;
import java.util.Map;

/** Owns the collection of accounts and looks one up by account number. */
public class Bank
{

    private final Map<String, Account> accounts = new HashMap<>();

    /**
     * Throws instead of returning null so a missing account can't slip through.
     * @param accountNumber 
     * @return account
     * @throws AccountNotFoundException 
     */
    public Account getAccount(String accountNumber)
        throws AccountNotFoundException
    {
        Account account =
            accountNumber == null ? null : accounts.get(accountNumber);
        if (account == null)
        {
            throw new AccountNotFoundException(
                "No account found with number '" + accountNumber + "'.");
        }
        return account;
    }


    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * @param accountNumber
     * @param initialBalanceCents
     * @return account
     * @throws DuplicateAccountException
     */
    public Account createAccount(String accountNumber, long initialBalanceCents)
        throws DuplicateAccountException
    {
        if (accountExists(accountNumber))
        {
            throw new DuplicateAccountException(
                "Account " + accountNumber + " already exists.");
        }
        Account account = new Account(accountNumber, initialBalanceCents);
        accounts.put(accountNumber, account);
        return account;
    }


    /** Exception-free existence check. 
     * @param accountNumber 
     * @return true  */
    public boolean accountExists(String accountNumber)
    {
        return accountNumber != null && accounts.containsKey(accountNumber);
    }
}
