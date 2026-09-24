package cashclash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CurrencyConverterTest
{

    private CurrencyConverter converter;

    @BeforeEach
    public void setUp()
    {
        converter = new CurrencyConverter();
        converter.setRate("EUR", 1.10); // fixed rate so the tests do not depend
                                        // on the defaults
    }


    @Test
    public void convertsKnownCurrencyUsingStoredRate()
        throws Exception
    {
        assertEquals(55.0, converter.convertToUSD(50.0, "EUR"), 0.0001);
    }


    @Test
    public void usdConvertsOneToOne()
        throws Exception
    {
        assertEquals(50.0, converter.convertToUSD(50.0, "USD"), 0.0001);
    }


    @Test
    public void currencyCodeIsCaseInsensitive()
        throws Exception
    {
        assertEquals(55.0, converter.convertToUSD(50.0, "eur"), 0.0001);
    }


    @Test
    public void unrecognizedCodeThrows()
    {
        assertThrows(
            UnsupportedCurrencyException.class,
            () -> converter.convertToUSD(50.0, "XYZ"));
    }


    @Test
    public void nullCodeThrows()
    {
        assertThrows(
            UnsupportedCurrencyException.class,
            () -> converter.convertToUSD(50.0, null));
    }


    @Test
    public void setRateAddsNewCurrency()
        throws Exception
    {
        assertFalse(converter.supports("CHF"));
        converter.setRate("CHF", 1.25);
        assertTrue(converter.supports("CHF"));
        assertEquals(125.0, converter.convertToUSD(100.0, "CHF"), 0.0001);
    }


    @Test
    public void setRateRejectsBadValues()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> converter.setRate("CHF", 0));
        assertThrows(
            IllegalArgumentException.class,
            () -> converter.setRate("CHF", -1));
        assertThrows(
            IllegalArgumentException.class,
            () -> converter.setRate("", 1.0));
        assertThrows(
            IllegalArgumentException.class,
            () -> converter.setRate(null, 1.0));
    }
}
