package ru.pepe.autosell;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ComboCounterTest {
    @Test void cyclesAfterEight() {
        ComboCounter counter = new ComboCounter();
        for (int cycle = 0; cycle < 20; cycle++) {
            for (int expected = 1; expected <= 8; expected++) assertEquals(expected, counter.next());
        }
    }
    @Test void disablingResetsSequence() {
        ComboCounter counter = new ComboCounter();
        counter.next(); counter.next(); counter.next();
        counter.reset();
        assertEquals(1, counter.next());
    }
    @Test void resetAfterFinaleStartsAtOne() {
        ComboCounter counter = new ComboCounter();
        for (int i = 0; i < 8; i++) counter.next();
        counter.reset();
        assertEquals(1, counter.next());
    }
}
