package cashclash;

import java.util.Locale;

/**
 * Tiny helper (not in the original design) so every class formats cents
 * the same way instead of each one duplicating the same String.format call.
 */
public final class Money {

    private Money() {
    }

    /** 123456 becomes "$1,234.56". */
    public static String format(long cents) {
        String sign = cents < 0 ? "-" : "";
        long abs = Math.abs(cents);
        return String.format(Locale.US, "%s$%,d.%02d", sign, abs / 100, abs % 100);
    }
}