package ru.pepe.autosell;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(EphemeralTestServerProvider.class)
class GuiVillagerFactoryTest {
    // The ephemeral server supplies registries, not a playable level. Bypass
    // construction to reproduce an unassigned client ID, but use the REAL
    // Entity.getId/setId methods -- these are the accessors in the crash stack.
    private static Villager unassignedModel() {
        return mock(Villager.class, CALLS_REAL_METHODS);
    }
    @Test void renderOnlyVillagerHasIdBeforeRendererUsesIt(MinecraftServer server) {
        var villager = unassignedModel();
        assertThrows(IllegalStateException.class, villager::getId);
        assertSame(villager, GuiVillagerFactory.prepareForRendering(villager));
        int id = assertDoesNotThrow(villager::getId);
        assertEquals(GuiVillagerFactory.RENDER_ID, id);
        assertTrue(id < 0);
        verify(villager, never()).level();
    }
    @Test void recreatingModelAfterModeResetStillAssignsId(MinecraftServer server) {
        var first = GuiVillagerFactory.prepareForRendering(unassignedModel());
        var next = GuiVillagerFactory.prepareForRendering(unassignedModel());
        assertNotSame(first, next);
        int id = assertDoesNotThrow(next::getId);
        assertEquals(first.getId(), id);
        verify(next, never()).level();
    }
}
