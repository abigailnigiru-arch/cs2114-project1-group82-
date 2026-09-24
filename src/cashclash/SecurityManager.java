package cashclash;

import java.util.HashMap;

/**
 * (Stretch) Stores and checks account passwords for double verification.
 *
 * Simple version: passwords are kept as plain text in a HashMap
 * (account number -> password). That is fine for a class project;
 * a real bank would hash them.
 */
public class SecurityManager {

    // account number -> password
    private HashMap<String, String> accountPasswords = new HashMap<>();

    /** Saves a password for the account (replaces the old one if there is one). 
     * @param accountNumber 
     * @param password */
    public void setPassword(String accountNumber, String password) {
        if (accountNumber == null || password == null || password.equals("")) {
            throw new IllegalArgumentException("Account number and password are required.");
        }
        accountPasswords.put(accountNumber, password);
    }

    /** Returns true if the attempt matches. A wrong password just returns false. 
     * @param accountNumber 
     * @param attempt 
     * @return true or false based on the scenario */
    public boolean verifyPassword(String accountNumber, String attempt) {
        if (!hasPassword(accountNumber) || attempt == null) {
            return false;
        }
        return accountPasswords.get(accountNumber).equals(attempt);
    }

    /** Returns true if this account has a password set. 
     * @param accountNumber 
     * @return true or false based on the scenario */
    public boolean hasPassword(String accountNumber) {
        return accountPasswords.containsKey(accountNumber);
    }
}
