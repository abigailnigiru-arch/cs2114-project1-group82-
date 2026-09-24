package cashclash;

/** Converts an amount in a named currency to US dollars. */
@FunctionalInterface
public interface CurrencyConverter
{
    /**
     * @param amount amount in the source currency
     * @param currencyCode ISO-style currency code, such as EUR
     * @return equivalent amount in US dollars
     * @throws UnsupportedCurrencyException if the code is not supported
     */
    double convertToUSD(double amount, String currencyCode)
        throws UnsupportedCurrencyException;
}