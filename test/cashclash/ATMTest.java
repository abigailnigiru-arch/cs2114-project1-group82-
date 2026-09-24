package cashclash;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ATMTest
{

    private Bank bank;
    private ATM atm;

    @BeforeEach
    public void setUp()
        throws Exception
    {
        bank = new Bank();
        Account account = bank.createAccount("12345", 10_000);   // $100.00
                                                                 // checking
        account.deposit(5_000, Account.SAVINGS);                  // $50.00
                                                                  // savings
        bank.createAccount("54321", 0);                           // brand-new,
                                                                  // empty
        atm = new ATM(bank);
    }

    // authenticate


    @Test
    public void authenticateStartsSessionForExistingAccount()
        throws Exception
    {
        assertTrue(atm.authenticate("12345"));
        assertTrue(atm.isSignedIn());
        assertEquals("12345", atm.getCurrentAccount().getAccountNumber());
    }


    @Test
    public void authenticateEmptyStringThrowsAndSessionDoesNotStart()
    {
        assertThrows(
            AccountNotFoundException.class,
            () -> atm.authenticate(""));
        assertFalse(atm.isSignedIn());
        assertNull(atm.getCurrentAccount());
    }


    @Test
    public void authenticateUnknownAccountThrows()
    {
        assertThrows(
            AccountNotFoundException.class,
            () -> atm.authenticate("99999"));
        assertFalse(atm.isSignedIn());
    }


    @Test
    public void authenticateMalformedNumberThrowsBeforeBankIsQueried()
    {
        assertThrows(
            AccountNotFoundException.class,
            () -> atm.authenticate("12-45"));
        assertFalse(atm.isSignedIn());
    }

    // handleDeposit


    @Test
    public void depositUpdatesBalanceAndReportsIt()
        throws Exception
    {
        atm.authenticate("12345");
        String message = atm.handleDeposit(1_000, "Checking");
        assertEquals(11_000, atm.getCurrentAccount().getCheckingBalance());
        assertTrue(message.contains("$110.00"));
    }


    @Test
    public void depositWhileSignedOutThrowsInsteadOfTouchingNullAccount()
    {
        assertThrows(
            IllegalStateException.class,
            () -> atm.handleDeposit(1_000, "Checking"));
    }

    // handleWithdrawal


    @Test
    public void withdrawalWithinLimitsUpdatesBalance()
        throws Exception
    {
        atm.authenticate("12345");
        String message = atm.handleWithdrawal(500, "Savings");
        assertEquals(4_500, atm.getCurrentAccount().getSavingsBalance());
        assertTrue(message.contains("$45.00"));
    }


    @Test
    public void overdrawThrowsAndSessionStaysOpen()
        throws Exception
    {
        atm.authenticate("12345");
        assertThrows(
            InsufficientFundsException.class,
            () -> atm.handleWithdrawal(999_999, "Savings"));
        assertTrue(atm.isSignedIn());
        assertEquals(5_000, atm.getCurrentAccount().getSavingsBalance());
    }


    @Test
    public void overDailyLimitThrowsDailyLimitExceeded()
        throws Exception
    {
        bank.getAccount("12345").deposit(100_000, Account.CHECKING);
        atm.authenticate("12345");
        assertThrows(
            DailyLimitExceededException.class,
            () -> atm.handleWithdrawal(
                Account.DAILY_WITHDRAWAL_LIMIT_CENTS + 1,
                "Checking"));
        assertTrue(atm.isSignedIn());
    }


    @Test
    public void withdrawalWhileSignedOutThrows()
    {
        assertThrows(
            IllegalStateException.class,
            () -> atm.handleWithdrawal(100, "Checking"));
    }

    // showAccountSummary


    @Test
    public void summaryShowsBothBalances()
        throws Exception
    {
        atm.authenticate("12345");
        String summary = atm.showAccountSummary();
        assertTrue(summary.contains("$100.00"));
        assertTrue(summary.contains("$50.00"));
    }


    @Test
    public void summaryForBrandNewAccountIsWellFormed()
        throws Exception
    {
        atm.authenticate("54321");
        String summary = atm.showAccountSummary();
        assertNotNull(summary);
        assertFalse(summary.trim().isEmpty());
        assertTrue(summary.contains("$0.00"));
    }

    // showHistory


    @Test
    public void historyListsPriorTransactionsInOrder()
        throws Exception
    {
        atm.authenticate("12345");
        atm.handleWithdrawal(100, "Checking");
        String history = atm.showHistory();
        assertTrue(history.contains("Deposit to Savings"));
        assertTrue(
            history.indexOf("Deposit to Savings") < history
                .indexOf("Withdrawal from Checking"));
    }


    @Test
    public void historyForBrandNewAccountSaysNoTransactions()
        throws Exception
    {
        atm.authenticate("54321");
        assertEquals("No transactions yet.", atm.showHistory());
    }

    // logout


    @Test
    public void logoutEndsSessionAndClearsAccount()
        throws Exception
    {
        atm.authenticate("12345");
        atm.logout();
        assertFalse(atm.isSignedIn());
        assertNull(atm.getCurrentAccount());
    }


    @Test
    public void logoutWhenNobodyIsSignedInIsANoOp()
    {
        assertDoesNotThrow(() -> atm.logout());
        assertFalse(atm.isSignedIn());
    }


    @Test
    public void actionsAfterLogoutAreRejected()
        throws Exception
    {
        atm.authenticate("12345");
        atm.logout();
        assertThrows(
            IllegalStateException.class,
            () -> atm.showAccountSummary());
    }

    // getRemainingDailyLimit


    @Test
    public void remainingLimitDelegatesToAccount()
        throws Exception
    {
        atm.authenticate("12345");
        atm.handleWithdrawal(10_000, "Checking");
        assertEquals(
            Account.DAILY_WITHDRAWAL_LIMIT_CENTS - 10_000,
            atm.getRemainingDailyLimit());
    }


    @Test
    public void remainingLimitIsFullBeforeAnyWithdrawal()
        throws Exception
    {
        atm.authenticate("12345");
        assertEquals(
            Account.DAILY_WITHDRAWAL_LIMIT_CENTS,
            atm.getRemainingDailyLimit());
    }


    @Test
    public void remainingLimitRequiresLogin()
    {
        assertThrows(
            IllegalStateException.class,
            () -> atm.getRemainingDailyLimit());
    }

    // createAccount


    @Test
    public void createAccountAddsItToTheBank()
        throws Exception
    {
        atm.createAccount("11111", 2_000, null);
        assertTrue(bank.accountExists("11111"));
        assertEquals(2_000, bank.getAccount("11111").getBalance());
    }


    @Test
    public void createAccountRejectsDuplicateAndBadFormat()
    {
        assertThrows(
            DuplicateAccountException.class,
            () -> atm.createAccount("12345", 0, null));
        assertThrows(
            IllegalArgumentException.class,
            () -> atm.createAccount("abc", 0, null));
    }

    // parseAmount (MVP: no converter)


    @Test
    public void parseAmountWithoutConverterIsUsdOnly()
        throws Exception
    {
        assertEquals(2_550, atm.parseAmount("25.50"));
        assertThrows(
            InvalidAmountException.class,
            () -> atm.parseAmount("50 EUR"));
    }
}
