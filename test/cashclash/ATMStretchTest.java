package cashclash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests the ATM together with the three stretch classes. */
public class ATMStretchTest
{

    private Bank bank;
    private SecurityManager security;
    private AlertSystem alerts;
    private ATM atm;

    @BeforeEach
    public void setUp()
        throws Exception
    {
        bank = new Bank();
        bank.createAccount("12345", 10_000);
        security = new SecurityManager();
        alerts = new AlertSystem();
        atm = new ATM(bank, security, alerts);
    }

    // double verification


    @Test
    public void accountWithoutPasswordLogsInNormally()
        throws Exception
    {
        assertFalse(atm.requiresPassword("12345"));
        assertTrue(atm.authenticate("12345"));
    }


    @Test
    public void accountWithPasswordNeedsTheRightOne()
        throws Exception
    {
        atm.createAccount("54321", 0, "secret");
        assertTrue(atm.requiresPassword("54321"));

        assertFalse(atm.authenticate("54321"));                 // none supplied
        assertFalse(atm.authenticate("54321", "wrong"));         // wrong
                                                                 // password
        assertFalse(atm.isSignedIn());

        assertTrue(atm.authenticate("54321", "secret"));         // correct
        assertTrue(atm.isSignedIn());
    }


    @Test
    public void failedLoginDoesNotLeaveAnOldSessionOpen()
        throws Exception
    {
        atm.createAccount("54321", 0, "secret");
        atm.authenticate("12345");
        assertFalse(atm.authenticate("54321", "wrong"));
        assertFalse(atm.isSignedIn());
    }

    // alert system


    @Test
    public void threeFailedWithdrawalsFlagTheAccount()
        throws Exception
    {
        atm.authenticate("12345");
        assertFalse(atm.isCurrentAccountFlagged());

        for (int i = 0; i < 3; i++)
        {
            assertThrows(
                InsufficientFundsException.class,
                () -> atm.handleWithdrawal(999_999, "Checking"));
        }
        assertEquals(3, alerts.getSuspiciousCount("12345"));
        assertTrue(atm.isCurrentAccountFlagged());
    }


    @Test
    public void overDailyLimitAlsoCountsAsSuspicious()
        throws Exception
    {
        bank.getAccount("12345").deposit(100_000, Account.CHECKING);
        atm.authenticate("12345");
        assertThrows(
            DailyLimitExceededException.class,
            () -> atm.handleWithdrawal(
                Account.DAILY_WITHDRAWAL_LIMIT_CENTS + 1,
                "Checking"));
        assertEquals(1, alerts.getSuspiciousCount("12345"));
    }


    @Test
    public void successfulWithdrawalIsNotSuspicious()
        throws Exception
    {
        atm.authenticate("12345");
        atm.handleWithdrawal(100, "Checking");
        assertEquals(0, alerts.getSuspiciousCount("12345"));
    }

    // feature flags


    @Test
    public void featureFlagsReflectWhatWasPassedIn()
    {
        assertTrue(atm.isSecurityEnabled());
        ATM plain = new ATM(bank);
        assertFalse(plain.isSecurityEnabled());
    }
}
