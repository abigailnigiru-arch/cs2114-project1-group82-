package cashclash;

import java.math.BigDecimal;
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
    private static final Pattern ACCOUNT_NUMBER = Pattern.compile("\\d{5}");

    // Euro, pound, yen and rupee signs written as unicode escapes so the file
    // compiles the same on every machine.
    private static final String SYMBOLS = "\u20AC\u00A3\u00A5\u20B9";

    // Something like "50 EUR", "EUR 50" or a currency sign: not US dollars.
    private static final Pattern FOREIGN_HINT = Pattern.compile(
        "(.*[" + SYMBOLS + "].*)|(.*\\d\\s*[A-Za-z]{3})|([A-Za-z]{3}\\s*\\d.*)");

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
        if (FOREIGN_HINT.matcher(s).matches()) {
            throw new InvalidAmountException("USD only: other currencies are not accepted.");
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