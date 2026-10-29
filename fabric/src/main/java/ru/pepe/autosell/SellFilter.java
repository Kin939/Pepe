package ru.pepe.autosell;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.slf4j.LoggerFactory;

public final class SellFilter {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config config = new Config();
    private static final class Config {
        boolean allowlist = false;
        boolean protectSpecialItems = true;
        List<String> items = List.of();
    }
    static void reload() {
        var path = FabricLoader.getInstance().getConfigDir().resolve("pepe-client.json");
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.writeString(path, GSON.toJson(config));
                return;
            }
            Config next = GSON.fromJson(Files.readString(path), Config.class);
            if (next == null || next.items == null || next.items.stream().anyMatch(id -> id == null
                    || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")))
                throw new IllegalArgumentException("items must contain Minecraft item IDs");
            config = next;
        } catch (Exception e) {
            LoggerFactory.getLogger("pepe").error("Cannot read {}; keeping last valid sell filter", path, e);
        }
    }
    public static boolean allowed(ItemStack stack) {
        if (stack.isEmpty()) return true;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return FilterPolicy.allowed(config.allowlist, config.items.contains(id), config.protectSpecialItems, isSpecial(stack));
    }
    static boolean isSpecial(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_NAME)
                || !stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()
                || !stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()
                || stack.has(DataComponents.CONTAINER) || stack.has(DataComponents.BUNDLE_CONTENTS)
                || stack.isDamaged();
    }
}
