package ru.pepe.autosell;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FilterPolicyTest {
    @Test void defaultFilterAllowsOrdinaryItems() {
        assertTrue(FilterPolicy.allowed(false, false, true, false));
    }
    @Test void blacklistRejectsListedItemsOnly() {
        assertFalse(FilterPolicy.allowed(false, true, false, false));
        assertTrue(FilterPolicy.allowed(false, false, false, false));
    }
    @Test void allowlistRejectsUnlistedItems() {
        assertTrue(FilterPolicy.allowed(true, true, true, false));
        assertFalse(FilterPolicy.allowed(true, false, true, false));
    }
    @Test void specialItemsStayProtectedEvenOnAllowlist() {
        assertFalse(FilterPolicy.allowed(false, false, true, true));
        assertFalse(FilterPolicy.allowed(true, true, true, true));
    }
    @Test void protectionCanBeDisabled() {
        assertTrue(FilterPolicy.allowed(false, false, false, true));
    }
}
