package ru.pepe.autosell;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

/** Only vanilla menu operations; no invented trades, prices, stock or server bypass. */
final class Seller {
    private enum Phase { FIND, FILL, SETTLE }
    private MerchantMenu menu;
    private Phase phase;
    private int age, wait, state, offerIndex, beforeEmeralds, outputPerTrade, completed;
    private final java.util.Set<Integer> exhausted = new java.util.HashSet<>();
    boolean active() { return menu != null; }

    void open(MerchantMenu next) {
        // A second interaction may replace a container; do not close the new one.
        menu = next;
        phase = Phase.FIND;
        age = wait = completed = 0;
        offerIndex = -1;
        exhausted.clear();
    }

    void tick() {
        if (menu == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null || mc.player.containerMenu != menu) { menu = null; return; }
        if (++age > 1200) { close(completed == 0); return; }
        if (phase == Phase.FIND) {
            // Offers arrive in a separate server packet after the open-screen packet.
            if (menu.getOffers().isEmpty()) { if (age > 60) close(completed == 0); return; }
            for (int i = 0; i < menu.getOffers().size(); i++) {
                MerchantOffer offer = menu.getOffers().get(i);
                if (!exhausted.contains(i) && eligible(offer) && fits(offer.getResult())) {
                    if (offerIndex == i && menu.getSlot(2).hasItem()
                            && menu.getOffers().get(i).satisfiedBy(menu.getSlot(0).getItem(), menu.getSlot(1).getItem())) {
                        take();
                        return;
                    }
                    offerIndex = i;
                    menu.setSelectionHint(i);
                    menu.tryMoveItems(i);
                    mc.getConnection().send(new ServerboundSelectTradePacket(i));
                    await(Phase.FILL);
                    return;
                }
            }
            close(completed == 0);
            return;
        }
        ++wait;
        // Wait for server menu synchronization, never spam a stale result slot.
        if (menu.getStateId() == state) {
            if (wait > 60) close(completed == 0);
            return;
        }
        if (phase == Phase.FILL) {
            MerchantOffer offer = menu.getOffers().get(offerIndex);
            if (offer.isOutOfStock() || !eligible(offer) || !menu.getSlot(2).hasItem() || !fits(offer.getResult())) {
                exhausted.add(offerIndex);
                phase = Phase.FIND;
                return;
            }
            if (!menu.getCarried().isEmpty()) { close(false); return; }
            take();
        } else {
            int gained = emeralds() - beforeEmeralds;
            if (gained > 0) {
                int trades = BatchResult.tradeCount(gained, outputPerTrade);
                completed += trades;
                Effects.sales(gained, trades);
            } else {
                exhausted.add(offerIndex);
            }
            // A quick-move consumes the filled input stacks. Refill and continue
            // the SAME villager session; do not require another right-click.
            phase = Phase.FIND;
        }
    }

    private void take() {
        beforeEmeralds = emeralds();
        outputPerTrade = menu.getOffers().get(offerIndex).getResult().getCount();
        click(2, ContainerInput.QUICK_MOVE); // Vanilla bulk-trade, directly into inventory.
        await(Phase.SETTLE);
    }

    private boolean eligible(MerchantOffer offer) {
        if (offer.isOutOfStock() || !offer.getResult().is(Items.EMERALD)) return false;
        ItemStack a = offer.getCostA(), b = offer.getCostB();
        if (a.is(Items.EMERALD) || b.is(Items.EMERALD) || !SellFilter.allowed(a) || !SellFilter.allowed(b)) return false;
        // Vanilla autofill searches the entire inventory: conservatively reject an offer if
        // a protected item of either input type exists, rather than risk selling it.
        int haveA = 0, haveB = 0;
        for (int i = 0; i < menu.slots.size(); i++) {
            if (i == 2) continue;
            ItemStack stack = menu.getSlot(i).getItem();
            if (stack.is(a.getItem()) || (!b.isEmpty() && stack.is(b.getItem()))) {
                if (!SellFilter.allowed(stack)) return false;
            }
            if (ItemStack.isSameItemSameComponents(stack, a)) haveA += stack.getCount();
            if (!b.isEmpty() && ItemStack.isSameItemSameComponents(stack, b)) haveB += stack.getCount();
        }
        if (!b.isEmpty() && ItemStack.isSameItemSameComponents(a, b)) return haveA >= a.getCount() + b.getCount();
        return haveA >= a.getCount() && haveB >= b.getCount();
    }
    private int destination(ItemStack result) {
        for (int i = 3; i < menu.slots.size(); i++) {
            ItemStack stack = menu.getSlot(i).getItem();
            if (stack.isEmpty() || (ItemStack.isSameItemSameComponents(stack, result)
                    && stack.getCount() + result.getCount() <= stack.getMaxStackSize())) return i;
        }
        return -1;
    }
    private boolean fits(ItemStack result) { return destination(result) >= 0; }
    private int emeralds() {
        int count = menu.getCarried().is(Items.EMERALD) ? menu.getCarried().getCount() : 0;
        for (int i = 3; i < menu.slots.size(); i++) {
            ItemStack stack = menu.getSlot(i).getItem();
            if (stack.is(Items.EMERALD)) count += stack.getCount();
        }
        return count;
    }
    private void await(Phase next) { phase = next; state = menu.getStateId(); wait = 0; }
    private void click(int slot, ContainerInput type) {
        var mc = Minecraft.getInstance();
        mc.gameMode.handleContainerInput(menu.containerId, slot, 0, type, mc.player);
    }
    void close(boolean error) {
        MerchantMenu old = menu;
        menu = null;
        var player = Minecraft.getInstance().player;
        if (old != null && player != null && player.containerMenu == old) player.closeContainer();
        if (error) Effects.play("error");
    }
}
