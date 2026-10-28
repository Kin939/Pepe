package ru.pepe.autosell;

import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
class TradeInputsTest {
    @Test void requiresFullPriceAcrossStacksAndUpdatesWithInventory(MinecraftServer server) {
        var price = new ItemStack(Items.WHEAT, 20);
        assertFalse(TradeInputs.canPay(price, ItemStack.EMPTY, List.of(), s -> true));
        assertFalse(TradeInputs.canPay(price, ItemStack.EMPTY, List.of(new ItemStack(Items.WHEAT, 19)), s -> true));
        assertTrue(TradeInputs.canPay(price, ItemStack.EMPTY,
                List.of(new ItemStack(Items.WHEAT, 12), new ItemStack(Items.WHEAT, 8)), s -> true));
    }
    @Test void bothCostsMustBeCoveredWithoutDoubleCounting(MinecraftServer server) {
        var a = new ItemStack(Items.WHEAT, 10);
        var b = new ItemStack(Items.PAPER, 2);
        assertFalse(TradeInputs.canPay(a, b, List.of(new ItemStack(Items.WHEAT, 64)), s -> true));
        assertTrue(TradeInputs.canPay(a, b,
                List.of(new ItemStack(Items.WHEAT, 10), new ItemStack(Items.PAPER, 2)), s -> true));
        assertFalse(TradeInputs.canPay(a, a, List.of(new ItemStack(Items.WHEAT, 10)), s -> true));
        assertTrue(TradeInputs.canPay(a, a, List.of(new ItemStack(Items.WHEAT, 20)), s -> true));
    }
    @Test void componentMismatchAndProtectedInputsCannotProduceGreen(MinecraftServer server) {
        var a = new ItemStack(Items.WHEAT, 10);
        var named = new ItemStack(Items.WHEAT, 64);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Keep me"));
        assertFalse(TradeInputs.canPay(a, ItemStack.EMPTY, List.of(named), s -> true));
        assertFalse(TradeInputs.canPay(a, ItemStack.EMPTY,
                List.of(new ItemStack(Items.WHEAT, 64), named), s -> !SellFilter.isSpecial(s)));
        assertFalse(TradeInputs.canPay(a, ItemStack.EMPTY,
                List.of(new ItemStack(Items.WHEAT, 64)), s -> s.isEmpty()));
    }
}
