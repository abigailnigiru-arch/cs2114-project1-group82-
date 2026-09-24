package cashclash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AlertSystemTest
{

    private AlertSystem alerts;

    @BeforeEach
    public void setUp()
    {
        alerts = new AlertSystem();
    }

    // recordSuspiciousActivity


    @Test
    public void firstEventCountsOneAndDoesNotFlag()
    {
        alerts.recordSuspiciousActivity("12345");
        assertEquals(1, alerts.getSuspiciousCount("12345"));
        assertFalse(alerts.isProblematic("12345"));
    }


    @Test
    public void secondEventStillNotFlagged()
    {
        alerts.recordSuspiciousActivity("12345");
        alerts.recordSuspiciousActivity("12345");
        assertEquals(2, alerts.getSuspiciousCount("12345"));
        assertFalse(alerts.isProblematic("12345"));
    }


    @Test
    public void thirdEventFlagsTheAccount()
    {
        for (int i = 0; i < AlertSystem.THRESHOLD; i++)
        {
            alerts.recordSuspiciousActivity("12345");
        }
        assertEquals(3, alerts.getSuspiciousCount("12345"));
        assertTrue(alerts.isProblematic("12345"));
    }


    @Test
    public void accountStaysFlaggedAfterMoreEvents()
    {
        for (int i = 0; i < 5; i++)
        {
            alerts.recordSuspiciousActivity("12345");
        }
        assertEquals(5, alerts.getSuspiciousCount("12345"));
        assertTrue(alerts.isProblematic("12345"));
    }


    @Test
    public void accountsAreTrackedIndependently()
    {
        for (int i = 0; i < 3; i++)
        {
            alerts.recordSuspiciousActivity("11111");
        }
        alerts.recordSuspiciousActivity("22222");
        assertTrue(alerts.isProblematic("11111"));
        assertFalse(alerts.isProblematic("22222"));
        assertEquals(1, alerts.getSuspiciousCount("22222"));
    }

    // isProblematic


    @Test
    public void neverReportedAccountIsNotProblematic()
    {
        assertFalse(alerts.isProblematic("99999"));
    }

    // getSuspiciousCount


    @Test
    public void neverReportedAccountHasCountZero()
    {
        assertEquals(0, alerts.getSuspiciousCount("99999"));
    }
}
