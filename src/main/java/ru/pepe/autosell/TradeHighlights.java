package ru.pepe.autosell;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.event.InputEvent;
import net.minecraft.world.phys.EntityHitResult;

/** Serialized, read-only vanilla trade probes. No item clicks or artificial restocks. */
final class TradeHighlights {
    private record Entry(TradeAvailability status, long checked) {}
    private final Map<UUID, Entry> cache = new HashMap<>();
    private boolean enabled;
    private long tick, nextRequest;
    private Villager pendingProbe, queuedManual;
    private MerchantMenu probeMenu;
    private UUID manualTarget;
    private int probeAge;

    void start() { stop(); enabled = true; }
    int outline(UUID id) {
        return enabled ? cache.getOrDefault(id, new Entry(TradeAvailability.UNKNOWN, 0)).status.color : 0;
    }
    UUID manualTarget() { return manualTarget; }

    void onInteract(InputEvent.InteractionKeyMappingTriggered event) {
        var mc = Minecraft.getInstance();
        if (!enabled || !event.isUseItem() || mc.player == null
                || !(mc.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Villager villager)) return;
        if (pendingProbe != null) {
            // Do not interleave two unidentified open-menu responses. Replay the
            // user's request as soon as the read-only probe finishes.
            queuedManual = villager;
            event.setCanceled(true);
            event.setSwingHand(false);
        } else {
            manualTarget = villager.getUUID();
            nextRequest = tick + 60; // Give this manual open response exclusive ownership.
        }
    }

    boolean acceptProbe(MerchantMenu menu) {
        if (!enabled || pendingProbe == null) return false;
        probeMenu = menu;
        return true;
    }

    void observe(UUID id, MerchantMenu menu) {
        if (!enabled || id == null || menu.getOffers().isEmpty()) return;
        int count = 0, available = 0;
        for (var offer : menu.getOffers()) {
            if (!offer.getResult().is(Items.EMERALD) || offer.getCostA().is(Items.EMERALD)
                    || offer.getCostB().is(Items.EMERALD)) continue;
            count++;
            if (!offer.isOutOfStock()) available++;
        }
        cache.put(id, new Entry(TradeAvailability.fromCounts(count, available), tick));
    }

    void tick(boolean selling) {
        if (!enabled) return;
        tick++;
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getConnection() == null) { stop(); return; }
        if (pendingProbe != null) {
            probeAge++;
            if (probeMenu != null && mc.player.containerMenu == probeMenu && !probeMenu.getOffers().isEmpty()) {
                observe(pendingProbe.getUUID(), probeMenu);
                finishProbe();
            } else if (probeAge >= 60 || !pendingProbe.isAlive()) {
                // Timeout is UNKNOWN, never falsely green/red. Back off this NPC.
                cache.put(pendingProbe.getUUID(), new Entry(TradeAvailability.UNKNOWN, tick));
                finishProbe();
            }
            return;
        }
        if (selling || mc.gui.getScreen() != null || mc.player.containerMenu != mc.player.inventoryMenu) return;
        if (queuedManual != null) {
            Villager target = queuedManual;
            queuedManual = null;
            if (reachable(target)) {
                manualTarget = target.getUUID();
                mc.getConnection().send(ServerboundInteractPacket.createInteractionPacket(target,
                        mc.player.isShiftKeyDown(), InteractionHand.MAIN_HAND));
                nextRequest = tick + 60;
            }
            return;
        }
        if (tick < nextRequest || mc.player.isShiftKeyDown() || !mc.player.getOffhandItem().isEmpty()) return;
        Villager chosen = null;
        long oldest = Long.MAX_VALUE;
        for (Villager candidate : mc.level.getEntitiesOfClass(Villager.class, mc.player.getBoundingBox().inflate(3))) {
            if (!reachable(candidate) || candidate.isBaby()) continue;
            Entry entry = cache.get(candidate.getUUID());
            long interval = entry != null && entry.status == TradeAvailability.EXHAUSTED ? 200 : 600;
            if (entry != null && tick - entry.checked < interval) continue;
            long checked = entry == null ? Long.MIN_VALUE : entry.checked;
            if (chosen == null || checked < oldest) { chosen = candidate; oldest = checked; }
        }
        if (chosen == null) return;
        pendingProbe = chosen;
        probeMenu = null;
        probeAge = 0;
        nextRequest = tick + 30; // At most one request per 1.5 seconds globally.
        // Empty OFF_HAND avoids applying a held name tag or another usable item.
        mc.getConnection().send(ServerboundInteractPacket.createInteractionPacket(chosen, false, InteractionHand.OFF_HAND));
    }

    private boolean reachable(Villager target) {
        var player = Minecraft.getInstance().player;
        double range = Math.min(2.5, player.entityInteractionRange());
        return target.isAlive() && player.distanceToSqr(target) <= range * range && player.hasLineOfSight(target);
    }

    private void finishProbe() {
        var player = Minecraft.getInstance().player;
        if (probeMenu != null && player != null && player.containerMenu == probeMenu) player.closeContainer();
        pendingProbe = null;
        probeMenu = null;
        probeAge = 0;
    }

    void stop() {
        enabled = false;
        finishProbe();
        queuedManual = null;
        manualTarget = null;
        cache.clear();
        tick = nextRequest = 0;
    }
}
