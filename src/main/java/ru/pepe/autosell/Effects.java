package ru.pepe.autosell;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class Effects {
    private static final SaleFeedback FEEDBACK = new SaleFeedback();
    private static final Identifier VIGNETTE = Identifier.fromNamespaceAndPath(Pepe.ID, "textures/gui/emerald_vignette.png");
    private static final Identifier[] FACES = java.util.Arrays.stream(SaleFeedback.Emotion.values())
            .map(e -> Identifier.fromNamespaceAndPath(Pepe.ID, "textures/gui/face_" + e.texture + ".png"))
            .toArray(Identifier[]::new);
    private static long visualStart;
    private static final Random RNG = new Random();
    private static final List<Toast> TOASTS = new ArrayList<>();
    private static final List<Drop> DROPS = new ArrayList<>();
    private static final List<Flyby> FLYBYS = new ArrayList<>();
    private static final SessionEarnings EARNINGS = new SessionEarnings();
    private static final Identifier VILLAGER_SPRITE = Identifier.fromNamespaceAndPath(Pepe.ID, "textures/gui/villager_flyby.png");
    private static int pendingFlybys, flybyCooldown, earningsPulse;
    private static final List<Clip> CLIPS = new ArrayList<>();
    private static final ItemStack EMERALD = new ItemStack(Items.EMERALD);
    private static Clip music;
    private static final ComboPlayback COMBO = new ComboPlayback();
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
    static void sales(int emeralds) {
        TOASTS.add(new Toast(emeralds));
        int stacks = EARNINGS.add(emeralds);
        pendingFlybys += stacks;
        FEEDBACK.sold(stacks > 0);
        earningsPulse = 20;
        play("cash");
    }
    static void beginVisuals() { visualStart = System.nanoTime(); }
    static float adjustFov(float original) {
        return original * SaleFeedback.zoomMultiplier((System.nanoTime() - visualStart) / 1_000_000_000.0);
    }
    static void failedTrade() { FEEDBACK.failed(); }
    static void villagerFinished() { COMBO.add(1); }
    private static void comboStep(int step) {
        shownCombo = step;
        comboAge = 0;
        play("combo_" + shownCombo);
        if (shownCombo == 8) {
            FEEDBACK.finale();
            for (int i = 0; i < 500; i++) DROPS.add(new Drop());
        }
    }
    static void tick() {
        clock++;
        FEEDBACK.tick();
        comboAge++;
        if (earningsPulse > 0) earningsPulse--;
        if (flybyCooldown > 0) flybyCooldown--;
        if (pendingFlybys > 0 && flybyCooldown == 0) {
            pendingFlybys--;
            flybyCooldown = 22;
            FLYBYS.add(new Flyby());
            play("musor_drop");
        }
        FLYBYS.removeIf(f -> ++f.age > FlybyMotion.LIFETIME);
        int step = COMBO.tick();
        if (step != 0) comboStep(step);
        TOASTS.removeIf(t -> ++t.age > 65);
        DROPS.removeIf(d -> ++d.age > d.life);
        CLIPS.removeIf(c -> !Minecraft.getInstance().getSoundManager().isActive(c));
    }
    static void clear() {
        FEEDBACK.clear();
        visualStart = 0;
        COMBO.clear();
        EARNINGS.reset();
        pendingFlybys = flybyCooldown = earningsPulse = 0;
        FLYBYS.clear();
        shownCombo = comboAge = 0;
        TOASTS.clear();
        DROPS.clear();
        for (Clip clip : CLIPS) Minecraft.getInstance().getSoundManager().stop(clip);
        CLIPS.clear();
    }
    static void render(GuiGraphicsExtractor g) {
        var mc = Minecraft.getInstance();
        int w = g.guiWidth(), h = g.guiHeight();
        // Screen effects underneath all Pepe HUD elements; center of vignette is transparent.
        g.pose().pushMatrix();
        try {
            g.pose().scale(w / 256f, h / 256f);
            g.blit(RenderPipelines.GUI_TEXTURED, VIGNETTE, 0, 0, 0, 0, 256, 256, 256, 256);
        } finally { g.pose().popMatrix(); }
        int flash = FEEDBACK.flashColor();
        if (flash != 0) g.fill(0, 0, w, h, flash);
        renderPortrait(g, h);
        for (int i = 0; i < TOASTS.size(); i++) {
            Toast t = TOASTS.get(TOASTS.size() - 1 - i);
            if (i > 7) break;
            String text = "+" + t.amount + " ✦";
            int x = w - mc.font.width(text) - 32, y = 18 + i * 24 - Math.min(t.age / 5, 9);
            int alpha = Math.min(255, (65 - t.age) * 15);
            g.item(EMERALD, x - 22, y - 4);
            g.text(mc.font, text, x, y, (alpha << 24) | 0x6BFFA0, true);
        }
        String earned = net.minecraft.network.chat.Component.translatable("hud.pepe.earned", EARNINGS.total()).getString();
        int panelWidth = mc.font.width(earned) + 42;
        int panelX = w - panelWidth - 10;
        int panelY = h - 35 - earningsPulse / 5;
        g.fill(panelX, panelY, w - 10, panelY + 25, 0xC0182922);
        g.item(EMERALD, panelX + 5, panelY + 4);
        g.text(mc.font, earned, panelX + 27, panelY + 8, 0xFF7CFFAC, true);
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
        for (Flyby f : FLYBYS) {
            float progress = FlybyMotion.progress(f.age);
            float x = FlybyMotion.x(f.age, w);
            float y = (float) (h * f.height + Math.sin(progress * Math.PI * 2) * h * 0.12);
            // Only a normal HUD texture: no Villager, world, entity ID, render
            // state extraction, InventoryScreen or mob-renderer mixins involved.
            g.pose().pushMatrix();
            try {
                g.pose().translate(x, y);
                g.pose().rotate(FlybyMotion.angle(f.age));
                g.pose().scale(2.5f, 2.5f);
                g.blit(RenderPipelines.GUI_TEXTURED, VILLAGER_SPRITE, -16, -32, 0, 0, 32, 64, 32, 64);
            } finally {
                g.pose().popMatrix();
            }
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
    private static void renderPortrait(GuiGraphicsExtractor g, int h) {
        var mc = Minecraft.getInstance();
        var mood = FEEDBACK.emotion();
        int x = 8, y = Math.max(8, h - 139);
        int border = mood.color;
        // Original CRT-inspired pixel bezel, not a game entity or third-party asset.
        g.fill(x + 4, y + 4, x + 94, y + 109, 0x66000000);
        g.fill(x - 2, y - 2, x + 92, y + 107, 0x292FE69C);
        g.fill(x, y, x + 90, y + 105, 0xFF09221B);
        g.fill(x + 2, y + 2, x + 88, y + 103, border);
        g.fill(x + 4, y + 4, x + 86, y + 101, 0xFF17392D);
        String title = "PEPE TV";
        g.text(mc.font, title, x + (90 - mc.font.width(title)) / 2, y + 7, 0xFFC8FFE0, true);
        int sx = x + 13, sy = y + 20;
        g.fill(sx - 2, sy - 2, sx + 66, sy + 66, 0xFF04120E);
        g.blit(RenderPipelines.GUI_TEXTURED, FACES[mood.ordinal()], sx, sy, 0, 0, 64, 64, 64, 64);
        for (int row = 0; row < 64; row += 3) g.fill(sx, sy + row, sx + 64, sy + row + 1, 0x1005120D);
        // Small animated indicator and two CRT knobs.
        g.fill(x + 6, y + 91, x + 9, y + 94, (clock / 12 % 2 == 0) ? border : 0xFF296E50);
        g.fill(x + 81, y + 91, x + 85, y + 95, 0xFF091A14);
        String label = net.minecraft.network.chat.Component.translatable("hud.pepe.face." + mood.texture).getString();
        g.pose().pushMatrix();
        try {
            g.pose().translate(x + 45f, y + 90f);
            g.pose().scale(0.75f, 0.75f);
            g.text(mc.font, label, -mc.font.width(label) / 2, 0, border, true);
        } finally { g.pose().popMatrix(); }
    }

    private static final class Toast {
        final int amount;
        int age;
        Toast(int amount) { this.amount = amount; }
    }
    private static final class Flyby {
        final float height = 0.25f + RNG.nextFloat() * 0.5f;
        int age;
    }
    private static final class Drop {
        final float x = RNG.nextFloat(), phase = RNG.nextFloat() * 6.28f;
        final float speed = 3 * (2 + RNG.nextFloat() * 5), spin = (RNG.nextFloat() - 0.5f) * 0.18f;
        final float scale = 0.5f + RNG.nextFloat() * 1.2f;
        final int delay = RNG.nextInt(65), life = delay + 60;
        int age;
    }
}
