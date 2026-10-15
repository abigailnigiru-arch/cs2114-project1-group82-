package cashclash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class InputValidatorTest
{

    // parseAmount: normal

    @Test
    public void parseAmountReturnsDollars()
        throws Exception
    {
        assertEquals(25.50, InputValidator.parseAmount("25.50"), 0.0001);
    }


    @Test
    public void parseAmountAcceptsDollarSignSpacesAndWholeNumbers()
        throws Exception
    {
        assertEquals(25.50, InputValidator.parseAmount("$25.50"), 0.0001);
        assertEquals(25.50, InputValidator.parseAmount("  25.5  "), 0.0001);
        assertEquals(40.0, InputValidator.parseAmount("40"), 0.0001);
    }


    @Test
    public void parseAmountCentsIsExact()
        throws Exception
    {
        assertEquals(10, InputValidator.parseAmountCents("0.10"));
        assertEquals(1999, InputValidator.parseAmountCents("19.99"));
        assertEquals(2550, InputValidator.parseAmountCents("$25.5"));
    }

    // parseAmount: bad input


    @Test
    public void nonNumericTextRejected()
    {
        assertThrows(
            InvalidAmountException.class,
            () -> InputValidator.parseAmount("twenty"));
        assertThrows(
            InvalidAmountException.class,
            () -> InputValidator.parseAmount("12abc"));
        assertThrows(
            InvalidAmountException.class,
            () -> InputValidator.parseAmount("1e3"));
    }


    @Test
    public void negativeAmountRejected()
    {
        assertThrows(
            InvalidAmountException.class,
            () -> InputValidator.parseAmount("-10.00"));
    }


    @Test
    public void zeroRejected()
    {
        assertThrows(
            InvalidAmountException.class,
            () -> InputValidator.parseAmount("0"));
        assertThrows(
            InvalidAmountException.class,
            () -> InputValidator.parseAmount("0.00"));
    }


    @Test
    public void foreignCurrencyRejectedWithUsdOnlyMessage()
    {
        InvalidAmountException symbol = assertThrows(
            InvalidAmountException.class,
            () -> InputValidator.parseAmount("\u20AC50"));
        assertTrue(symbol.getMessage().contains("USD only"));

        InvalidAmountException code = assertThrows(
            InvalidAmountException.class,
            () -> InputValidator.parseAmount("50 EUR"));
        assertTrue(code.getMessage().contains("USD only"));
    }


    @Test
    public void emptyAndNullRejected()
    {
        assertThrows(
            InvalidAmountException.class,
            () -> InputValidator.parseAmount(""));
        assertThrows(
            InvalidAmountException.class,
            () -> InputValidator.parseAmount("   "));
        assertThrows(
            InvalidAmountException.class,
            () -> InputValidator.parseAmount(null));
    }


    @Test
    public void moreThanTwoDecimalPlacesRejected()
    {
        assertThrows(
            InvalidAmountException.class,
            () -> InputValidator.parseAmount("1.234"));
    }


    @Test
    public void absurdlyLargeAmountRejected()
    {
        assertThrows(
            InvalidAmountException.class,
            () -> InputValidator.parseAmount("99999999999999999999999"));
    }

    // isValidAccountNumber


    @Test
    public void fiveDigitAccountNumberIsValid()
    {
        assertTrue(InputValidator.isValidAccountNumber("12345"));
    }


    @Test
    public void malformedAccountNumbersAreInvalid()
    {
        assertFalse(InputValidator.isValidAccountNumber("12-45"));
        assertFalse(InputValidator.isValidAccountNumber("1234"));
        assertFalse(InputValidator.isValidAccountNumber("123456"));
        assertFalse(InputValidator.isValidAccountNumber("abcde"));
        assertFalse(InputValidator.isValidAccountNumber(""));
        assertFalse(InputValidator.isValidAccountNumber(null));
    }

}
