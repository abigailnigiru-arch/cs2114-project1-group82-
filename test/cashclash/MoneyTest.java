package cashclash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MoneyTest
{

    @Test
    public void formatsDollarsAndCents()
    {
        assertEquals("$1,234.56", Money.format(123_456));
    }


    @Test
    public void padsSingleDigitCents()
    {
        assertEquals("$0.05", Money.format(5));
        assertEquals("$10.07", Money.format(1_007));
    }


    @Test
    public void formatsZero()
    {
        assertEquals("$0.00", Money.format(0));
    }


    @Test
    public void formatsVeryLargeAmounts()
    {
        assertEquals("$999,999.99", Money.format(99_999_999L));
    }


    @Test
    public void formatsNegativeAmounts()
    {
        assertEquals("-$1.50", Money.format(-150));
    }
}
