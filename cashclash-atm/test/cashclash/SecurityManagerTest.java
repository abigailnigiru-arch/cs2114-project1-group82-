package cashclash;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc" })
public class SecurityManagerTest
{

    private SecurityManager security;

    @BeforeEach
    public void setUp()
    {
        security = new SecurityManager();
    }

    // setPassword / hasPassword


    @Test
    public void setPasswordMakesHasPasswordTrue()
    {
        assertFalse(security.hasPassword("12345"));
        security.setPassword("12345", "secret");
        assertTrue(security.hasPassword("12345"));
    }


    @Test
    public void hasPasswordFalseForAccountThatNeverOptedIn()
    {
        assertFalse(security.hasPassword("99999"));
        assertFalse(security.hasPassword(null));
    }


    @Test
    public void settingAgainOverwritesInsteadOfThrowing()
    {
        security.setPassword("12345", "old");
        assertDoesNotThrow(() -> security.setPassword("12345", "new"));
        assertFalse(security.verifyPassword("12345", "old"));
        assertTrue(security.verifyPassword("12345", "new"));
    }


    @Test
    public void emptyOrNullPasswordRejected()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> security.setPassword("12345", ""));
        assertThrows(
            IllegalArgumentException.class,
            () -> security.setPassword("12345", null));
        assertThrows(
            IllegalArgumentException.class,
            () -> security.setPassword(null, "x"));
    }

    // verifyPassword


    @Test
    public void correctPasswordReturnsTrue()
    {
        security.setPassword("12345", "secret");
        assertTrue(security.verifyPassword("12345", "secret"));
    }


    @Test
    public void wrongPasswordReturnsFalseWithoutThrowing()
    {
        security.setPassword("12345", "secret");
        assertDoesNotThrow(() -> security.verifyPassword("12345", "wrong"));
        assertFalse(security.verifyPassword("12345", "wrong"));
    }


    @Test
    public void passwordsAreCaseSensitive()
    {
        security.setPassword("12345", "Secret");
        assertFalse(security.verifyPassword("12345", "secret"));
    }


    @Test
    public void verifyFalseForUnknownAccountOrNullAttempt()
    {
        assertFalse(security.verifyPassword("99999", "anything"));
        security.setPassword("12345", "secret");
        assertFalse(security.verifyPassword("12345", null));
    }


    @Test
    public void samePasswordOnTwoAccountsDoesNotCrossVerify()
    {
        security.setPassword("11111", "shared");
        assertFalse(security.verifyPassword("22222", "shared"));
    }
}
