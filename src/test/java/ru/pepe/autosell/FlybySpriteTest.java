package ru.pepe.autosell;

import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FlybySpriteTest {
    @Test void hudContainsNoEntityRendererDependencies() throws Exception {
        // Inspect the built class without loading/initializing client classes in
        // a server-side test. Guard against reintroducing the crashing 3D path.
        try (var in = getClass().getClassLoader().getResourceAsStream("ru/pepe/autosell/Effects.class")) {
            assertNotNull(in);
            String pool = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
            assertFalse(pool.contains("net/minecraft/world/entity/"));
            assertFalse(pool.contains("net/minecraft/client/gui/screens/inventory/InventoryScreen"));
            assertFalse(pool.contains("GuiVillagerFactory"));
            assertFalse(pool.contains("extractRenderState"));
            assertTrue(pool.contains("textures/gui/villager_flyby.png"));
        }
    }
    @Test void spriteIsBundledTransparentRgbaPng() throws Exception {
        try (var raw = getClass().getClassLoader().getResourceAsStream("assets/pepe/textures/gui/villager_flyby.png")) {
            assertNotNull(raw);
            var in = new DataInputStream(raw);
            assertEquals(0x89504E470D0A1A0AL, in.readLong());
            assertEquals(13, in.readInt());
            assertEquals(0x49484452, in.readInt());
            assertEquals(32, in.readInt());
            assertEquals(64, in.readInt());
            assertEquals(8, in.readUnsignedByte());
            assertEquals(6, in.readUnsignedByte()); // RGBA, not a missing/opaque placeholder
        }
    }
    @Test void spriteCrossesScreenAndSpinsTwice() {
        assertEquals(-140f, FlybyMotion.x(0, 800));
        assertEquals(940f, FlybyMotion.x(FlybyMotion.LIFETIME, 800));
        assertEquals(0f, FlybyMotion.angle(0));
        assertEquals((float) (Math.PI * 4), FlybyMotion.angle(FlybyMotion.LIFETIME));
        assertEquals(0f, FlybyMotion.progress(-20));
        assertEquals(1f, FlybyMotion.progress(500));
    }
}
