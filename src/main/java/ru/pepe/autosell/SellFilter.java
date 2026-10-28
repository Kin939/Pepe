package ru.pepe.autosell;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
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
        if (ALLOWLIST.get() != ITEMS.get().contains(id)) return false;
        return !PROTECT.get() || !(stack.has(DataComponents.CUSTOM_NAME)
                || stack.has(DataComponents.ENCHANTMENTS) || stack.has(DataComponents.STORED_ENCHANTMENTS)
                || stack.has(DataComponents.CONTAINER) || stack.has(DataComponents.BUNDLE_CONTENTS)
                || stack.isDamaged());
    }
}
