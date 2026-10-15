package cashclash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class TransactionTest
{

    @Test
    public void constructorStoresAllFields()
    {
        LocalDateTime before = LocalDateTime.now();
        Transaction t =
            new Transaction("Deposit", 5_000, "Checking", "Initial deposit");
        LocalDateTime after = LocalDateTime.now();

        assertEquals("Deposit", t.getType());
        assertEquals(5_000, t.getAmount());
        assertEquals("Checking", t.getAccountType());
        assertEquals("Initial deposit", t.getDescription());
        assertNotNull(t.getTimestamp());
        assertFalse(t.getTimestamp().isBefore(before));
        assertFalse(t.getTimestamp().isAfter(after));
    }


    @Test
    public void zeroAmountRejected()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction("Deposit", 0, "Checking", "nothing"));
    }


    @Test
    public void negativeAmountRejected()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction("Withdrawal", -100, "Savings", "negative"));
    }


    @Test
    public void nullTypeOrAccountTypeRejected()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction(null, 100, "Checking", "x"));
        assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction("Deposit", 100, null, "x"));
    }


    @Test
    public void nullDescriptionBecomesEmpty()
    {
        assertEquals(
            "",
            new Transaction("Deposit", 100, "Checking", null).getDescription());
    }


    @Test
    public void toStringIsReadable()
    {
        String text =
            new Transaction("Deposit", 5_000, "Checking", "Initial deposit")
                .toString();
        assertTrue(text.contains("Deposit"));
        assertTrue(text.contains("Checking"));
        assertTrue(text.contains("$50.00"));
        assertTrue(text.contains("Initial deposit"));
    }


    @Test
    public void toStringHandlesVeryLargeAmount()
    {
        String text = new Transaction("Deposit", 99_999_999L, "Savings", "Big")
            .toString();
        assertTrue(text.contains("$999,999.99"));
    }
}
