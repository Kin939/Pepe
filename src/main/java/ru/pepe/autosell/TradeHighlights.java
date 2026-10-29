package ru.pepe.autosell;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.world.item.ItemStack;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Items;

/** Serialized, read-only vanilla trade probes. No item clicks or artificial restocks. */
final class TradeHighlights {
    private record OfferSnapshot(ItemStack a, ItemStack b, boolean stocked) {}
    private record Entry(TradeAvailability status, long checked, List<OfferSnapshot> offers) {}
    private final Map<UUID, Entry> cache = new HashMap<>();
    private boolean enabled;
    private long tick, nextRequest;
    private Villager pendingProbe, queuedManual;
    private MerchantMenu probeMenu;
    private UUID manualTarget;
    private int probeAge;

    void start() { stop(); enabled = true; }
    int outline(UUID id) {
        return enabled ? cache.getOrDefault(id, new Entry(TradeAvailability.UNKNOWN, 0, List.of())).status.color : 0;
    }
    UUID manualTarget() { return manualTarget; }

    /** Called before a manual interaction packet is sent. True means defer it. */
    boolean onInteract(Villager villager) {
        if (!enabled) return false;
        if (pendingProbe != null) {
            queuedManual = villager;
            return true;
        }
        manualTarget = villager.getUUID();
        nextRequest = tick + 60;
        return false;
    }

    boolean acceptProbe(MerchantMenu menu) {
        if (!enabled || pendingProbe == null) return false;
        probeMenu = menu;
        return true;
    }

    void observe(UUID id, MerchantMenu menu) {
        if (!enabled || id == null || menu.getOffers().isEmpty()) return;
        var offers = new ArrayList<OfferSnapshot>();
        for (var offer : menu.getOffers()) {
            if (!offer.getResult().is(Items.EMERALD) || offer.getCostA().is(Items.EMERALD)
                    || offer.getCostB().is(Items.EMERALD)) continue;
            offers.add(new OfferSnapshot(offer.getCostA().copy(), offer.getCostB().copy(), !offer.isOutOfStock()));
        }
        cache.put(id, new Entry(status(offers, inventory()), tick, List.copyOf(offers)));
    }

    private List<ItemStack> inventory() {
        var player = Minecraft.getInstance().player;
        var inputs = new ArrayList<ItemStack>();
        if (player == null) return inputs;
        // Only the 36 slots vanilla trade autofill can consume: no armor/offhand.
        for (int i = 0; i < 36; i++) inputs.add(player.getInventory().getItem(i));
        if (player.containerMenu instanceof MerchantMenu menu) {
            inputs.add(menu.getSlot(0).getItem());
            inputs.add(menu.getSlot(1).getItem());
        }
        return inputs;
    }

    private TradeAvailability status(List<OfferSnapshot> offers, List<ItemStack> inputs) {
        int payable = 0;
        for (var offer : offers) {
            if (offer.stocked && TradeInputs.canPay(offer.a, offer.b, inputs, SellFilter::allowed)) payable++;
        }
        return TradeAvailability.fromCounts(offers.size(), payable);
    }

    void tick(boolean selling) {
        if (!enabled) return;
        tick++;
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getConnection() == null) { stop(); return; }
        var inputs = inventory();
        cache.replaceAll((id, entry) -> entry.offers.isEmpty() ? entry
                : new Entry(status(entry.offers, inputs), entry.checked, entry.offers));
        if (pendingProbe != null) {
            probeAge++;
            if (probeMenu != null && mc.player.containerMenu == probeMenu && !probeMenu.getOffers().isEmpty()) {
                observe(pendingProbe.getUUID(), probeMenu);
                finishProbe();
            } else if (probeAge >= 60 || !pendingProbe.isAlive()) {
                // Preserve the last server snapshot on timeout. Back off this NPC.
                UUID id = pendingProbe.getUUID();
                Entry previous = cache.get(id);
                cache.put(id, new Entry(previous == null ? TradeAvailability.UNKNOWN : previous.status, tick,
                        previous == null ? List.of() : previous.offers));
                finishProbe();
            }
            return;
        }
        if (selling || mc.gui.screen() != null || mc.player.containerMenu != mc.player.inventoryMenu) return;
        if (queuedManual != null) {
            Villager target = queuedManual;
            queuedManual = null;
            if (reachable(target)) {
                manualTarget = target.getUUID();
                mc.getConnection().send(new ServerboundInteractPacket(target.getId(), InteractionHand.MAIN_HAND,
                        target.getBoundingBox().getCenter(), mc.player.isShiftKeyDown()));
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
        mc.getConnection().send(new ServerboundInteractPacket(chosen.getId(), InteractionHand.OFF_HAND, chosen.getBoundingBox().getCenter(), false));
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
