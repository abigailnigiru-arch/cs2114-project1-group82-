package cashclash;

/** Indicates that a currency converter does not support a currency code. */
public class UnsupportedCurrencyException extends Exception
{
    private static final long serialVersionUID = 1L;

    public UnsupportedCurrencyException(String message)
    {
        super(message);
    }
}