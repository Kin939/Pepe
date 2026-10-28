package ru.pepe.autosell;

import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
class GuiVillagerFactoryTest {
    @Test void renderOnlyVillagerHasIdBeforeRendererUsesIt(MinecraftServer server) {
        var level = server.overworld();
        var villager = GuiVillagerFactory.create(level);
        assertEquals(GuiVillagerFactory.RENDER_ID, assertDoesNotThrow(villager::getId));
        assertTrue(villager.getId() < 0);
        assertNull(level.getEntity(villager.getId()), "GUI model must never be spawned in the world");
    }
    @Test void recreatingModelAfterModeResetStillAssignsId(MinecraftServer server) {
        var first = GuiVillagerFactory.create(server.overworld());
        var next = GuiVillagerFactory.create(server.overworld());
        assertNotSame(first, next);
        assertEquals(first.getId(), assertDoesNotThrow(next::getId));
        assertNull(server.overworld().getEntity(next.getId()));
    }
}
