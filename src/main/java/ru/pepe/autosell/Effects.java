package ru.pepe.autosell;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class Effects {
    private static final Random RNG = new Random();
    private static final List<Toast> TOASTS = new ArrayList<>();
    private static final List<Drop> DROPS = new ArrayList<>();
    private static final List<Clip> CLIPS = new ArrayList<>();
    private static final ItemStack EMERALD = new ItemStack(Items.EMERALD);
    private static Clip music;
    private static final ComboCounter COMBO = new ComboCounter();
    private static int shownCombo, comboAge;
    private static long clock;

    private static final class Clip extends AbstractTickableSoundInstance {
        Clip(String name, boolean loop) {
            super(SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Pepe.ID, name)),
                    loop ? SoundSource.MUSIC : SoundSource.PLAYERS, RandomSource.create());
            relative = true;
            attenuation = Attenuation.NONE;
            volume = loop ? 0.55f : 0.8f;
            looping = loop;
            delay = 0;
        }
        @Override public void tick() {}
    }
    static void play(String name) {
        Clip clip = new Clip(name, false);
        CLIPS.add(clip);
        Minecraft.getInstance().getSoundManager().play(clip);
    }
    static void startMusic() {
        stopMusic();
        // Never pretend a different recording is the requested AI cover.
        var mc = Minecraft.getInstance();
        if (mc.getResourceManager().getResource(Identifier.fromNamespaceAndPath(Pepe.ID, "sounds/music.ogg")).isEmpty()) return;
        music = new Clip("music", true);
        mc.getSoundManager().play(music);
    }
    static void stopMusic() {
        if (music != null) Minecraft.getInstance().getSoundManager().stop(music);
        music = null;
    }
    static void sale(int emeralds) {
        shownCombo = COMBO.next();
        comboAge = 0;
        TOASTS.add(new Toast(emeralds));
        play("cash");
        play("combo_" + shownCombo);
        if (shownCombo == 8) {
            for (int i = 0; i < 500; i++) DROPS.add(new Drop());
        }
    }
    static void tick() {
        clock++;
        comboAge++;
        TOASTS.removeIf(t -> ++t.age > 65);
        DROPS.removeIf(d -> ++d.age > d.life);
        CLIPS.removeIf(c -> !Minecraft.getInstance().getSoundManager().isActive(c));
    }
    static void clear() {
        COMBO.reset();
        shownCombo = comboAge = 0;
        TOASTS.clear();
        DROPS.clear();
        for (Clip clip : CLIPS) Minecraft.getInstance().getSoundManager().stop(clip);
        CLIPS.clear();
    }
    static void render(GuiGraphicsExtractor g) {
        var mc = Minecraft.getInstance();
        int w = g.guiWidth(), h = g.guiHeight();
        g.text(mc.font, "AUTO SELL [C]", 9, 9, 0xFF70FF99, true);
        for (int i = 0; i < TOASTS.size(); i++) {
            Toast t = TOASTS.get(TOASTS.size() - 1 - i);
            if (i > 7) break;
            String text = "+" + t.amount + " ✦";
            int x = w - mc.font.width(text) - 32, y = 18 + i * 24 - Math.min(t.age / 5, 9);
            int alpha = Math.min(255, (65 - t.age) * 15);
            g.item(EMERALD, x - 22, y - 4);
            g.text(mc.font, text, x, y, (alpha << 24) | 0x6BFFA0, true);
        }
        if (shownCombo > 0 && comboAge < 50) {
            String text = "Combo " + shownCombo;
            g.pose().pushMatrix();
            g.pose().translate(w / 2f + 22, h / 2f + 20);
            g.pose().rotate((float) (Math.sin(comboAge * 0.65) * 0.22 * Math.exp(-comboAge / 22.0)));
            float scale = 1.35f + (float) Math.exp(-comboAge / 8.0) * 0.65f;
            g.pose().scale(scale, scale);
            int x = 0;
            for (int i = 0; i < text.length(); i++) {
                String ch = text.substring(i, i + 1);
                int color = shownCombo == 8 ? Color.HSBtoRGB((clock * 0.035f + i * 0.12f) % 1, 0.9f, 1) : 0xFFFFDA55;
                g.text(mc.font, ch, x, 0, color, true);
                x += mc.font.width(ch);
            }
            g.pose().popMatrix();
        }
        for (Drop d : DROPS) {
            if (d.age < d.delay) continue;
            float x = (d.x * w + (float) Math.sin((d.age + d.phase) * 0.065) * 18);
            float y = -24 + (d.age - d.delay) * d.speed * (h / 280f);
            if (y > h + 24) continue;
            g.pose().pushMatrix();
            g.pose().translate(x, y);
            g.pose().rotate(d.phase + d.age * d.spin);
            g.pose().scale(d.scale, d.scale);
            g.item(EMERALD, -8, -8);
            g.pose().popMatrix();
        }
    }
    private static final class Toast {
        final int amount;
        int age;
        Toast(int amount) { this.amount = amount; }
    }
    private static final class Drop {
        final float x = RNG.nextFloat(), phase = RNG.nextFloat() * 6.28f;
        final float speed = 2 + RNG.nextFloat() * 5, spin = (RNG.nextFloat() - 0.5f) * 0.18f;
        final float scale = 0.5f + RNG.nextFloat() * 1.2f;
        final int delay = RNG.nextInt(65), life = delay + 180;
        int age;
    }
}
