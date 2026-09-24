import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// -------------------------------------------------------------------------
/**
 *  Tests the SecurityManager Class, making sure that passwords are verified
 *  
 *  @author shreyasnath
 *  @version Sep 23, 2026
 */
public class SecurityManagerTest {

    private SecurityManager security;

    // ----------------------------------------------------------
    /**
     * Starts a fresh SecurityManager instance before each test execution
     */
    @BeforeEach
    public void setUp() {
        security = new SecurityManager();
    }

    // setPassword / hasPassword

    // ----------------------------------------------------------
    /**
     * Verifies setting a password for an account updates hasPassword to true
     */
    @Test
    public void setPasswordMakesHasPasswordTrue() {
        assertFalse(security.hasPassword("12345"));
        security.setPassword("12345", "secret");
        assertTrue(security.hasPassword("12345"));
    }

    // ----------------------------------------------------------
    /**
     * Verifies hasPassword returns false for non-existent or null account IDs
     */
    @Test
    public void hasPasswordFalseForAccountThatNeverOptedIn() {
        assertFalse(security.hasPassword("99999"));
        assertFalse(security.hasPassword(null));
    }

    // ----------------------------------------------------------
    /**
     * Verifies calling setPassword again overwrites the 
     * existing password without throwing an exception
     */
    @Test
    public void settingAgainOverwritesInsteadOfThrowing() {
        security.setPassword("12345", "old");
        assertDoesNotThrow(() -> security.setPassword("12345", "new"));
        assertFalse(security.verifyPassword("12345", "old"));
        assertTrue(security.verifyPassword("12345", "new"));
    }

    // ----------------------------------------------------------
    /**
     * Verifies setPassword throws an IllegalArgumentException when given empty or null parameters
     */
    @Test
    public void emptyOrNullPasswordRejected() {
        assertThrows(IllegalArgumentException.class, () -> security.setPassword("12345", ""));
        assertThrows(IllegalArgumentException.class, () -> security.setPassword("12345", null));
        assertThrows(IllegalArgumentException.class, () -> security.setPassword(null, "x"));
    }

    // verifyPassword

    // ----------------------------------------------------------
    /**
     * Verifies an account with the matching password returns true
     */
    @Test
    public void correctPasswordReturnsTrue() {
        security.setPassword("12345", "secret");
        assertTrue(security.verifyPassword("12345", "secret"));
    }

    // ----------------------------------------------------------
    /**
     * Verifies providing an incorrect password returns false
     */
    @Test
    public void wrongPasswordReturnsFalseWithoutThrowing() {
        security.setPassword("12345", "secret");
        assertDoesNotThrow(() -> security.verifyPassword("12345", "wrong"));
        assertFalse(security.verifyPassword("12345", "wrong"));
    }

    // ----------------------------------------------------------
    /**
     * Verifies password comparison is case-sensitive during verification
     */
    @Test
    public void passwordsAreCaseSensitive() {
        security.setPassword("12345", "Secret");
        assertFalse(security.verifyPassword("12345", "secret"));
    }

    // ----------------------------------------------------------
    /**
     * Verifies verification fails when given an unknown account or a null password input
     */
    @Test
    public void verifyFalseForUnknownAccountOrNullAttempt() {
        assertFalse(security.verifyPassword("99999", "anything"));
        security.setPassword("12345", "secret");
        assertFalse(security.verifyPassword("12345", null));
    }

    // ----------------------------------------------------------
    /**
     * Verifies that credentials are bound to their respective account IDs
     */
    @Test
    public void samePasswordOnTwoAccountsDoesNotCrossVerify() {
        security.setPassword("11111", "shared");
        assertFalse(security.verifyPassword("22222", "shared"));
    }
}
