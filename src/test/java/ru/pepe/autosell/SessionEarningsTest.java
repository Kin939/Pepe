package ru.pepe.autosell;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionEarningsTest {
    @Test void stackCrossingsIncludeRemaindersAndMultipleStacks() {
        var earnings = new SessionEarnings();
        assertEquals(0, earnings.add(63));
        assertEquals(1, earnings.add(1));
        assertEquals(2, earnings.add(130));
        assertEquals(194, earnings.total());
        assertEquals(0, earnings.add(0));
        assertEquals(0, earnings.add(-5));
        assertEquals(194, earnings.total());
    }
    @Test void resetStartsNewModeAtZeroWithFreshMilestones() {
        var earnings = new SessionEarnings();
        earnings.add(63);
        earnings.reset();
        assertEquals(0, earnings.total());
        assertEquals(0, earnings.add(1));
        assertEquals(1, earnings.add(63));
    }
    @Test void profitableVisitEarnsOneComboNotOnePerTrade() {
        var visit = new VillagerVisit();
        for (int i = 0; i < 12; i++) visit.sold();
        assertTrue(visit.finish());
        assertFalse(visit.finish());
    }
    @Test void emptyVisitsDoNotAdvanceCombo() {
        assertFalse(new VillagerVisit().finish());
    }
    @Test void eightProfitableVillagersReachFinale() {
        var playback = new ComboPlayback();
        for (int i = 0; i < 8; i++) {
            var visit = new VillagerVisit();
            for (int trade = 0; trade < 12; trade++) visit.sold();
            if (visit.finish()) playback.add(1);
        }
        var steps = new java.util.ArrayList<Integer>();
        for (int i = 0; i < 200; i++) {
            int step = playback.tick();
            if (step > 0) steps.add(step);
        }
        assertEquals(java.util.List.of(1,2,3,4,5,6,7,8), steps);
    }
}
