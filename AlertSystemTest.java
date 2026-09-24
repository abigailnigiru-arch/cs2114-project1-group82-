import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// -------------------------------------------------------------------------
/**
 *  Tests the AlertSystem Class, making sure that account suspicious activity counts 
 *  are recorded and evaluated against the flagging threshold
 *
 *  @author shreyasnath
 *  @version Sep 23, 2026
 */
public class AlertSystemTest {

    private AlertSystem alerts;

    // ----------------------------------------------------------
    /**
     * Starts a fresh AlertSystem instance before each test execution
     */
    @BeforeEach
    public void setUp() {
        alerts = new AlertSystem();
    }

    // recordSuspiciousActivity

    // ----------------------------------------------------------
    /**
     * Verifies recording a single suspicious event to 1 without it being problematic
     */
    @Test
    public void firstEventCountsOneAndDoesNotFlag() {
        alerts.recordSuspiciousActivity("12345");
        assertEquals(1, alerts.getSuspiciousCount("12345"));
        assertFalse(alerts.isProblematic("12345"));
    }

    // ----------------------------------------------------------
    /**
     * Verifies recording two suspicious events to 2 without it being problematic
     */
    @Test
    public void secondEventStillNotFlagged() {
        alerts.recordSuspiciousActivity("12345");
        alerts.recordSuspiciousActivity("12345");
        assertEquals(2, alerts.getSuspiciousCount("12345"));
        assertFalse(alerts.isProblematic("12345"));
    }

    // ----------------------------------------------------------
    /**
     * Verifies reaching the threshold of suspicious events correctly
     */
    @Test
    public void thirdEventFlagsTheAccount() {
        for (int i = 0; i < AlertSystem.THRESHOLD; i++) {
            alerts.recordSuspiciousActivity("12345");
        }
        assertEquals(3, alerts.getSuspiciousCount("12345"));
        assertTrue(alerts.isProblematic("12345"));
    }

    // ----------------------------------------------------------
    /**
     * Verifies an account remains flagged as problematic after 3 counts
     */
    @Test
    public void accountStaysFlaggedAfterMoreEvents() {
        for (int i = 0; i < 5; i++) {
            alerts.recordSuspiciousActivity("12345");
        }
        assertEquals(5, alerts.getSuspiciousCount("12345"));
        assertTrue(alerts.isProblematic("12345"));
    }

    // ----------------------------------------------------------
    /**
     * Verifies that they are maintained from different accounts
     */
    @Test
    public void accountsAreTrackedIndependently() {
        for (int i = 0; i < 3; i++) {
            alerts.recordSuspiciousActivity("11111");
        }
        alerts.recordSuspiciousActivity("22222");
        assertTrue(alerts.isProblematic("11111"));
        assertFalse(alerts.isProblematic("22222"));
        assertEquals(1, alerts.getSuspiciousCount("22222"));
    }

    // isProblematic

    // ----------------------------------------------------------
    /**
     * Verifies an account with no recorded activity returns false when asked if problematic
     */
    @Test
    public void neverReportedAccountIsNotProblematic() {
        assertFalse(alerts.isProblematic("99999"));
    }

    // getSuspiciousCount

    // ----------------------------------------------------------
    /**
     * Verifies an account with no recorded activity returns 0
     */
    @Test
    public void neverReportedAccountHasCountZero() {
        assertEquals(0, alerts.getSuspiciousCount("99999"));
    }
}
