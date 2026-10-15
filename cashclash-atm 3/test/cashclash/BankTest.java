package cashclash;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BankTest {

    private Bank bank;

    @BeforeEach
    public void setUp() throws Exception {
        bank = new Bank();
        bank.createAccount("12345", 10_000);
    }

    // getAccount

    @Test
    public void getAccountReturnsExistingAccount() throws Exception {
        Account account = bank.getAccount("12345");
        assertNotNull(account);
        assertEquals("12345", account.getAccountNumber());
    }

    @Test
    public void getAccountThrowsForMissingAccount() {
        assertThrows(AccountNotFoundException.class, () -> bank.getAccount("99999"));
    }

    @Test
    public void getAccountThrowsForNullAndEmpty() {
        assertThrows(AccountNotFoundException.class, () -> bank.getAccount(null));
        assertThrows(AccountNotFoundException.class, () -> bank.getAccount(""));
    }

    // createAccount

    @Test
    public void createAccountReturnsNewAccountWithBalance() throws Exception {
        Account created = bank.createAccount("54321", 10_000);
        assertEquals(10_000, created.getBalance());
        assertSame(created, bank.getAccount("54321"));
    }

    @Test
    public void createAccountThrowsOnDuplicate() throws Exception {
        bank.createAccount("54321", 10_000);
        assertThrows(DuplicateAccountException.class, () -> bank.createAccount("54321", 10_000));
    }

    // accountExists

    @Test
    public void accountExistsTrueForCreatedAccount() {
        assertTrue(bank.accountExists("12345"));
    }

    @Test
    public void accountExistsFalseWithoutThrowing() {
        assertFalse(bank.accountExists("00000"));
        assertFalse(bank.accountExists(null));
    }
}
