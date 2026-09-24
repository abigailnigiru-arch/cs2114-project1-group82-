package cashclash;

import javax.swing.SwingUtilities;

/** Wires everything together and opens the GUI. Run this class. */
public class Main
{

    // ----------------------------------------------------------
    /**
     * Place a description of your method here.
     * @param args
     */
    public static void main(String[] args)
    {
        Bank bank = new Bank();
        try
        {
            // demo account: $1,000.00 checking and $500.00 savings, no password
            Account demo = bank.createAccount("12345", 100_000);
            demo.deposit(50_000, Account.SAVINGS);
        }
        catch (DuplicateAccountException e)
        {
            throw new IllegalStateException(e); // cannot happen on a brand-new
                                                // Bank
        }

        // Stretch goals: pass null in place of either collaborator to switch
        // that feature off.
        ATM atm = new ATM(
            bank,
            new AlertSystem(),
            new SecurityManager());

        SwingUtilities.invokeLater(() -> new ATMGui(atm).setVisible(true));
    }
}
