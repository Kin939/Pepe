package ru.pepe.autosell;

import net.minecraft.server.MinecraftServer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
class SellFilterTest {
    @Test void ordinaryTradeInputsAreNotSpecial(MinecraftServer server) {
        for (var item : new net.minecraft.world.item.Item[] {Items.PUMPKIN, Items.WHEAT, Items.PAPER, Items.ROTTEN_FLESH}) {
            assertFalse(SellFilter.isSpecial(new ItemStack(item)), "Normal trade input was protected: " + item);
        }
    }
    @Test void emptyEnchantmentComponentsAreNotEnchantments(MinecraftServer server) {
        ItemStack stack = new ItemStack(Items.PUMPKIN);
        stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        stack.set(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        assertTrue(stack.has(DataComponents.ENCHANTMENTS));
        assertFalse(SellFilter.isSpecial(stack));
    }
    @Test void actuallyEnchantedItemsStayProtected(MinecraftServer server) {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        stack.enchant(server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.UNBREAKING), 1);
        assertTrue(SellFilter.isSpecial(stack));
    }
    @Test void namedItemsStayProtected(MinecraftServer server) {
        ItemStack stack = new ItemStack(Items.PUMPKIN);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Do not sell"));
        assertTrue(SellFilter.isSpecial(stack));
    }
}
