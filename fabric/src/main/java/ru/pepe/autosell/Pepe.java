package ru.pepe.autosell;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;

public final class Pepe implements ClientModInitializer {
    public static final String ID = "pepe";
    private static Pepe instance;
    private final TradeHighlights highlights = new TradeHighlights();
    private final Seller seller = new Seller(highlights);
    private KeyMapping toggle;
    private boolean enabled;
    private int smokeTicks;

    @Override public void onInitializeClient() {
        instance = this;
        SellFilter.reload();
        toggle = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.pepe.toggle", InputConstants.KEY_C, KeyMapping.Category.MISC));
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (enabled && player == Minecraft.getInstance().player && entity instanceof Villager villager
                    && highlights.onInteract(villager)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(ID, "effects"), (graphics, delta) -> {
            if (enabled) Effects.render(graphics);
        });
    }

    /** Screen interception is before init/render; server menu stays installed. */
    public static boolean interceptScreen(Screen next) {
        if (instance == null) return false;
        if (instance.enabled && next instanceof MerchantScreen screen) {
            if (!instance.highlights.acceptProbe(screen.getMenu()))
                instance.seller.open(screen.getMenu(), instance.highlights.manualTarget());
            return true;
        }
        if (instance.seller.active() && next != null) instance.seller.close(false);
        return false;
    }

    public static int outline(java.util.UUID id) {
        return instance == null ? 0 : instance.highlights.outline(id);
    }

    public static float adjustFov(float original) {
        var mc = Minecraft.getInstance();
        return instance != null && instance.enabled && mc.level != null && mc.gui.screen() == null
                ? Effects.adjustFov(original) : original;
    }

    private void tick(Minecraft mc) {
        // Opt-in CI startup check; no test behavior in normal launches.
        if (Boolean.getBoolean("pepe.smokeTest") && mc.gui.screen() instanceof TitleScreen && ++smokeTicks >= 60) {
            try { java.nio.file.Files.writeString(java.nio.file.Path.of("pepe-smoke-ok"), "Fabric client reached title screen"); }
            catch (java.io.IOException e) { throw new IllegalStateException(e); }
            mc.stop();
        }
        if (mc.player == null || mc.level == null) {
            if (enabled) disable(false);
            return;
        }
        while (toggle.consumeClick()) {
            if (enabled) disable(true);
            else {
                SellFilter.reload();
                Effects.clear();
                enabled = true;
                Effects.beginVisuals();
                highlights.start();
                Effects.startMusic();
                mc.player.sendSystemMessage(Component.translatable("message.pepe.on"));
            }
        }
        if (enabled) {
            seller.tick();
            highlights.tick(seller.active());
            Effects.tick();
        }
    }

    private void disable(boolean sound) {
        enabled = false;
        highlights.stop();
        seller.close(false);
        Effects.clear();
        Effects.stopMusic();
        if (sound) {
            Effects.play("off");
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("message.pepe.off"));
        }
    }
}
