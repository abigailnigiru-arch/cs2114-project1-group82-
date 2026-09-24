package cashclash;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses and validates raw console/GUI text before anything else sees it.
 * Stateless: every method is static.
 *
 */
public final class InputValidator {

    private static final String NUM = "(\\d+(?:\\.\\d{1,2})?|\\.\\d{1,2})";
    private static final Pattern PLAIN = Pattern.compile("^" + NUM + "$");
    private static final Pattern NEGATIVE = Pattern.compile("^-\\s*\\d*\\.?\\d+$");
    private static final Pattern TOO_MANY_DECIMALS = Pattern.compile("^\\d*\\.\\d{3,}$");
    private static final Pattern SYMBOL_PREFIX = Pattern.compile(
        "^([\\u20AC\\u00A3\\u00A5\\u20B9])\\s*" + NUM + "$");
    private static final Pattern CODE_SUFFIX = Pattern.compile(
        "^" + NUM + "\\s*([A-Za-z]{3})$");
    private static final Pattern CODE_PREFIX = Pattern.compile(
        "^([A-Za-z]{3})\\s*" + NUM + "$");
    private static final Pattern ACCOUNT_NUMBER = Pattern.compile("\\d{5}");
    private static final Map<Character, String> SYMBOL_CODES = new HashMap<>();

    static {
        SYMBOL_CODES.put('\u20AC', "EUR");
        SYMBOL_CODES.put('\u00A3', "GBP");
        SYMBOL_CODES.put('\u00A5', "JPY");
        SYMBOL_CODES.put('\u20B9', "INR");
    }

    private InputValidator() {
    }

    /** Parses a USD amount such as "25.50" or "$25.50" into dollars. */
    public static double parseAmount(String raw) throws InvalidAmountException {
        return parseAmountCents(raw) / 100.0;
    }

    /** Same rules as parseAmount, but returns exact cents (no floating-point drift). */
    public static long parseAmountCents(String raw) throws InvalidAmountException {
        return parseUsd(raw);
    }

    /** Parses USD or converts a recognized foreign-currency amount to cents. */
    public static long parseAmountCents(String raw, CurrencyConverter converter)
        throws InvalidAmountException, UnsupportedCurrencyException
    {
        if (converter != null && raw != null)
        {
            String value = raw.trim();
            String currency = null;
            String number = null;
            Matcher matcher;
            if ((matcher = SYMBOL_PREFIX.matcher(value)).matches())
            {
                currency = SYMBOL_CODES.get(matcher.group(1).charAt(0));
                number = matcher.group(2);
            }
            else if ((matcher = CODE_SUFFIX.matcher(value)).matches())
            {
                currency = matcher.group(2).toUpperCase();
                number = matcher.group(1);
            }
            else if ((matcher = CODE_PREFIX.matcher(value)).matches())
            {
                currency = matcher.group(1).toUpperCase();
                number = matcher.group(2);
            }
            if (currency != null && !currency.equals("USD"))
            {
                double usd = converter.convertToUSD(
                    Double.parseDouble(number), currency);
                long cents = Math.round(usd * 100);
                if (cents <= 0)
                {
                    throw new InvalidAmountException(
                        "Amount is too small once converted to USD.");
                }
                return cents;
            }
        }
        return parseUsd(raw);
    }

    /** True for exactly five digits, e.g. "12345". */
    public static boolean isValidAccountNumber(String raw) {
        return raw != null && ACCOUNT_NUMBER.matcher(raw).matches();
    }

    private static long parseUsd(String raw) throws InvalidAmountException {
        if (raw == null) {
            throw new InvalidAmountException("Please enter an amount.");
        }
        String s = raw.trim();
        s = s.replaceFirst("(?i)^usd\\s*", "").replaceFirst("(?i)\\s*usd$", "");
        if (s.startsWith("$")) {
            s = s.substring(1).trim();
        }
        if (s.isEmpty()) {
            throw new InvalidAmountException("Please enter an amount.");
        }
        if (s.matches(".*[\\u20AC\\u00A3\\u00A5\\u20B9].*")
            || s.matches(".*\\d\\s*[A-Za-z]{3}")
            || s.matches("[A-Za-z]{3}\\s*\\d.*")) {
            throw new InvalidAmountException(
                "USD only: other currencies are not accepted.");
        }
        if (NEGATIVE.matcher(s).matches()) {
            throw new InvalidAmountException("Amount cannot be negative.");
        }
        if (TOO_MANY_DECIMALS.matcher(s).matches()) {
            throw new InvalidAmountException("Use at most two decimal places.");
        }
        if (!PLAIN.matcher(s).matches()) {
            throw new InvalidAmountException("'" + raw.trim() + "' is not a valid number.");
        }
        long cents;
        try {
            cents = new BigDecimal(s).movePointRight(2).longValueExact();
        } catch (ArithmeticException e) {
            throw new InvalidAmountException("That amount is too large.");
        }
        if (cents <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
        return cents;
    }
}