package ru.pepe.autosell;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.config.ModConfig;
import com.mojang.blaze3d.platform.InputConstants;

@Mod(value = Pepe.ID, dist = Dist.CLIENT)
public final class Pepe {
    public static final String ID = "pepe";
    private final KeyMapping toggle = new KeyMapping("key.pepe.toggle", InputConstants.KEY_C, KeyMapping.Category.MISC);
    private final TradeHighlights highlights = new TradeHighlights();
    private final Seller seller = new Seller(highlights);
    private boolean enabled;

    public Pepe(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SellFilter.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        bus.addListener(this::keys);
        bus.addListener(this::highlightModifiers);
        NeoForge.EVENT_BUS.addListener(this::interact);
        NeoForge.EVENT_BUS.addListener(this::tick);
        NeoForge.EVENT_BUS.addListener(this::opening);
        NeoForge.EVENT_BUS.addListener(this::render);
        NeoForge.EVENT_BUS.addListener(this::fov);
    }

    private void interact(net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
        var mc = Minecraft.getInstance();
        if (!enabled || !event.isUseItem() || mc.player == null
                || !(mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit)
                || !(hit.getEntity() instanceof net.minecraft.world.entity.npc.villager.Villager villager)) return;
        if (highlights.onInteract(villager)) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    private void keys(RegisterKeyMappingsEvent event) { event.register(toggle); }

    private void highlightModifiers(net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(net.minecraft.client.renderer.entity.VillagerRenderer.class, (villager, state) -> {
            int color = highlights.outline(villager.getUUID());
            if (color != 0) state.outlineColor = color;
        });
    }

    private void opening(ScreenEvent.Opening event) {
        if (enabled && event.getNewScreen() instanceof MerchantScreen screen) {
            // The server container is already installed; only suppress its visual screen.
            if (!highlights.acceptProbe(screen.getMenu())) seller.open(screen.getMenu(), highlights.manualTarget());
            event.setCanceled(true);
        } else if (seller.active() && event.getNewScreen() != null) {
            seller.close(false);
        }
    }

    private void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            if (enabled) disable(false);
            return;
        }
        while (toggle.consumeClick()) {
            if (enabled) disable(true);
            else {
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

    private void fov(net.neoforged.neoforge.client.event.ViewportEvent.ComputeFov event) {
        var mc = Minecraft.getInstance();
        if (enabled && mc.level != null && mc.gui.screen() == null)
            event.setFOV(Effects.adjustFov(event.getFOV()));
    }

    private void render(RenderGuiEvent.Post event) {
        if (enabled) Effects.render(event.getGuiGraphics());
    }
}
