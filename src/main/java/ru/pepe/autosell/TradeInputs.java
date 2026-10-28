package ru.pepe.autosell;

import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

/** Same conservative input rules for the seller and inventory-aware outlines. */
final class TradeInputs {
    static boolean canPay(ItemStack a, ItemStack b, Iterable<ItemStack> inventory, Predicate<ItemStack> allowed) {
        if (!allowed.test(a) || !allowed.test(b)) return false;
        int haveA = 0, haveB = 0;
        for (ItemStack stack : inventory) {
            if ((stack.is(a.getItem()) || (!b.isEmpty() && stack.is(b.getItem()))) && !allowed.test(stack)) return false;
            if (ItemStack.isSameItemSameComponents(stack, a)) haveA += stack.getCount();
            if (!b.isEmpty() && ItemStack.isSameItemSameComponents(stack, b)) haveB += stack.getCount();
        }
        if (!b.isEmpty() && ItemStack.isSameItemSameComponents(a, b)) return haveA >= a.getCount() + b.getCount();
        return haveA >= a.getCount() && haveB >= b.getCount();
    }
}
