package ru.pepe.autosell;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class SellFilter {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue ALLOWLIST;
    private static final ModConfigSpec.BooleanValue PROTECT;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEMS;
    static {
        var b = new ModConfigSpec.Builder();
        ALLOWLIST = b.comment("false: listed items are excluded. true: ONLY listed items may be sold.").define("allowlist", false);
        ITEMS = b.comment("Item IDs, for example minecraft:pumpkin, minecraft:wheat.")
                .defineListAllowEmpty("items", List.of(), () -> "minecraft:pumpkin", v -> v instanceof String s && s.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"));
        PROTECT = b.comment("Never sell named, enchanted, damaged or container items.")
                .define("protectSpecialItems", true);
        SPEC = b.build();
    }
    public static boolean allowed(ItemStack stack) {
        if (stack.isEmpty()) return true;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return FilterPolicy.allowed(ALLOWLIST.get(), ITEMS.get().contains(id), PROTECT.get(), isSpecial(stack));
    }

    static boolean isSpecial(ItemStack stack) {
        // ENCHANTMENTS is a default component of ordinary items too. Presence
        // alone does NOT mean enchanted (pumpkins, wheat, etc. have EMPTY).
        return stack.has(DataComponents.CUSTOM_NAME)
                || !stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()
                || !stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()
                || stack.has(DataComponents.CONTAINER) || stack.has(DataComponents.BUNDLE_CONTENTS)
                || stack.isDamaged();
    }
}
