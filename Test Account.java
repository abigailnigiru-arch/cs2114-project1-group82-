import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AccountTest {

    private Account account;

    @BeforeEach
    public void setUp() {
        account = new Account("12345", 100_000); // $1,000.00 in checking
    }

    // getBalance

    @Test
    public void getBalanceReturnsInitialBalance() {
        assertEquals(100_000, account.getBalance());
    }

    @Test
    public void newEmptyAccountHasZeroBalance() {
        assertEquals(0, new Account("00001", 0).getBalance());
    }

    @Test
    public void negativeInitialBalanceRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Account("00001", -1));
    }

    @Test
    public void getBalanceIsCheckingPlusSavings() {
        account.deposit(2_500, Account.SAVINGS);
        assertEquals(102_500, account.getBalance());
        assertEquals(100_000, account.getCheckingBalance());
        assertEquals(2_500, account.getSavingsBalance());
    }

    // deposit

    @Test
    public void depositIncreasesBalanceAndLogsOneTransaction() {
        Account small = new Account("00002", 1_000);
        small.deposit(500);
        assertEquals(1_500, small.getBalance());
        assertEquals(1, small.getTransactionHistory().size());
        assertEquals("Deposit", small.getTransactionHistory().get(0).getType());
    }

    @Test
    public void depositToSavingsGoesToSavings() {
        account.deposit(700, Account.SAVINGS);
        assertEquals(700, account.getBalance(Account.SAVINGS));
        assertEquals(Account.SAVINGS, account.getTransactionHistory().get(0).getAccountType());
    }

    @Test
    public void depositNegativeOrZeroThrowsAndLeavesBalanceAlone() {
        assertThrows(IllegalArgumentException.class, () -> account.deposit(-500));
        assertThrows(IllegalArgumentException.class, () -> account.deposit(0));
        assertEquals(100_000, account.getBalance());
        assertTrue(account.getTransactionHistory().isEmpty());
    }

    @Test
    public void unknownAccountTypeRejected() {
        assertThrows(IllegalArgumentException.class, () -> account.deposit(100, "Brokerage"));
    }

    // withdraw: balance path

    @Test
    public void withdrawReducesBalanceAndLogsOneTransaction() throws Exception {
        Account small = new Account("00002", 1_000);
        small.withdraw(200);
        assertEquals(800, small.getBalance());
        assertEquals(1, small.getTransactionHistory().size());
        assertEquals("Withdrawal", small.getTransactionHistory().get(0).getType());
    }

    @Test
    public void withdrawMoreThanBalanceThrowsAndLeavesBalanceAlone() {
        Account small = new Account("00002", 1_000);
        assertThrows(InsufficientFundsException.class, () -> small.withdraw(5_000));
        assertEquals(1_000, small.getBalance());
        assertTrue(small.getTransactionHistory().isEmpty());
    }

    @Test
    public void withdrawChecksTheChosenAccountTypeNotTheTotal() {
        account.deposit(5_000, Account.SAVINGS);
        // total is $1,050 but savings only holds $50
        assertThrows(InsufficientFundsException.class,
            () -> account.withdraw(10_000, Account.SAVINGS));
        assertEquals(5_000, account.getSavingsBalance());
    }

    @Test
    public void withdrawNegativeOrZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(-1));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(0));
    }

    // withdraw: daily-limit path

    @Test
    public void withdrawUnderDailyLimitIncreasesDailyTotal() throws Exception {
        account.withdraw(100);
        assertEquals(Account.DAILY_WITHDRAWAL_LIMIT_CENTS - 100, account.getRemainingDailyLimit());
    }

    @Test
    public void withdrawOverDailyLimitThrowsEvenWithEnoughBalance() throws Exception {
        account.withdraw(Account.DAILY_WITHDRAWAL_LIMIT_CENTS); // exactly the limit is allowed
        assertThrows(DailyLimitExceededException.class, () -> account.withdraw(1));
        assertEquals(100_000 - Account.DAILY_WITHDRAWAL_LIMIT_CENTS, account.getBalance());
    }

    @Test
    public void dailyLimitCountsCheckingAndSavingsTogether() throws Exception {
        account.deposit(50_000, Account.SAVINGS);
        account.withdraw(30_000, Account.CHECKING);
        assertThrows(DailyLimitExceededException.class,
            () -> account.withdraw(20_001, Account.SAVINGS));
    }

    // getTransactionHistory

    @Test
    public void historyIsInChronologicalOrder() throws Exception {
        account.deposit(100);
        account.withdraw(200);
        account.deposit(300);
        List<Transaction> history = account.getTransactionHistory();
        assertEquals(3, history.size());
        assertEquals(100, history.get(0).getAmount());
        assertEquals(200, history.get(1).getAmount());
        assertEquals(300, history.get(2).getAmount());
    }

    @Test
    public void newAccountHistoryIsEmptyNotNull() {
        assertTrue(account.getTransactionHistory().isEmpty());
    }

    @Test
    public void historyListCannotBeModifiedFromOutside() {
        account.deposit(100);
        assertThrows(UnsupportedOperationException.class,
            () -> account.getTransactionHistory().clear());
    }

    // resetDailyWithdrawalIfNewDay

    @Test
    public void newDayResetsDailyTotal() throws Exception {
        account.withdraw(20_000);
        account.setDailyWithdrawalDateForTesting(LocalDate.now().minusDays(1));
        account.resetDailyWithdrawalIfNewDay();
        assertEquals(Account.DAILY_WITHDRAWAL_LIMIT_CENTS, account.getRemainingDailyLimit());
    }

    @Test
    public void resetTwiceOnSameDayIsANoOp() throws Exception {
        account.withdraw(20_000);
        account.resetDailyWithdrawalIfNewDay();
        account.resetDailyWithdrawalIfNewDay();
        assertEquals(Account.DAILY_WITHDRAWAL_LIMIT_CENTS - 20_000, account.getRemainingDailyLimit());
    }

    // getRemainingDailyLimit

    @Test
    public void remainingLimitIsLimitMinusWithdrawn() throws Exception {
        account.withdraw(20_000);
        assertEquals(30_000, account.getRemainingDailyLimit());
    }

    @Test
    public void remainingLimitIsZeroNotNegativeAtTheLimit() throws Exception {
        account.withdraw(Account.DAILY_WITHDRAWAL_LIMIT_CENTS);
        assertEquals(0, account.getRemainingDailyLimit());
    }

    @Test
    public void remainingLimitIsFullBeforeAnyWithdrawal() {
        assertEquals(Account.DAILY_WITHDRAWAL_LIMIT_CENTS, account.getRemainingDailyLimit());
    }
}
