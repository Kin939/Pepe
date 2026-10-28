package ru.pepe.autosell;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SellFilterTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
    @Test void ordinaryTradeInputsAreNotSpecial() {
        for (var item : new net.minecraft.world.item.Item[] {Items.PUMPKIN, Items.WHEAT, Items.PAPER, Items.ROTTEN_FLESH}) {
            assertFalse(SellFilter.isSpecial(new ItemStack(item)), "Normal trade input was protected: " + item);
        }
    }
    @Test void emptyEnchantmentComponentsAreNotEnchantments() {
        ItemStack stack = new ItemStack(Items.PUMPKIN);
        stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        stack.set(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        assertFalse(SellFilter.isSpecial(stack));
    }
    @Test void namedItemsStayProtected() {
        ItemStack stack = new ItemStack(Items.PUMPKIN);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Do not sell"));
        assertTrue(SellFilter.isSpecial(stack));
    }
}
