package ru.pepe.autosell;

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

final class Effects {
    private static final SaleFeedback FEEDBACK = new SaleFeedback();
    private static final Identifier VIGNETTE = Identifier.fromNamespaceAndPath(Pepe.ID, "textures/gui/emerald_vignette.png");
    private static final java.util.Map<String, Identifier> FACES = java.util.Arrays.stream(PortraitArt.NAMES)
            .collect(java.util.stream.Collectors.toUnmodifiableMap(n -> n,
                    n -> Identifier.fromNamespaceAndPath(Pepe.ID, "textures/gui/face_" + n + ".png")));
    private static final Identifier[] FLYBY_FRAMES = new Identifier[8];
    private static final ImpactZoom ZOOM = new ImpactZoom();
    private static int reactions, cashCooldown;
    private static Clip comboClip, jackpotClip;
    static {
        for (int i=0;i<8;i++) FLYBY_FRAMES[i] = Identifier.fromNamespaceAndPath(Pepe.ID,
                "textures/gui/" + (i==0 ? "villager_flyby.png" : "villager_flyby_"+i+".png"));
    }
    private static long visualStart, lastTickNanos;
    private static int reactionPulse;
    private static double displayedEarnings;
    private static final int MAX_SPARKS = 144, MAX_RINGS = 6;
    private static final List<Spark> SPARKS = new ArrayList<>();
    private static final List<Ring> RINGS = new ArrayList<>();
    private static final Random RNG = new Random();
    private static final List<Toast> TOASTS = new ArrayList<>();
    private static final List<Drop> DROPS = new ArrayList<>();
    private static final List<Flyby> FLYBYS = new ArrayList<>();
    private static final SessionEarnings EARNINGS = new SessionEarnings();
    private static final Identifier VILLAGER_SPRITE = Identifier.fromNamespaceAndPath(Pepe.ID, "textures/gui/villager_flyby.png");
    private static int pendingFlybys, flybyCooldown, earningsPulse;
    private static final List<Clip> CLIPS = new ArrayList<>();
    private static final Identifier EMERALD = Identifier.fromNamespaceAndPath("minecraft", "textures/item/emerald.png");
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
            volume = loop ? 0.55f : name.equals("cash") ? 0.35f : 0.8f;
            looping = loop;
            delay = 0;
        }
        void gain(float target) { volume += (target - volume) * 0.15f; }
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
        if (emeralds <= 0) return;
        if (TOASTS.size() == 8) TOASTS.remove(0);
        TOASTS.add(new Toast(emeralds));
        int stacks = EARNINGS.add(emeralds);
        pendingFlybys += stacks;
        FEEDBACK.sold(false);
        reactions++;
        reactionPulse = 20;
        burst(0.87f, 0.8f, 14, 0xFF69FFD0);
        ring(0.87f, 0.8f, 0xFF69FFD0, 0.55f);
        earningsPulse = 20;
        if (cashCooldown == 0) { play("cash"); cashCooldown = 4; }
    }
    static void beginVisuals() {
        visualStart = System.nanoTime();
        burst(0.14f, 0.75f, 18, 0xFF5DFFD0);
    }
    static float adjustFov(float original) {
        return original * ZOOM.multiplier();
    }
    static void failedTrade() {
        FEEDBACK.failed();
        reactions++;
        reactionPulse = 20;
        burst(0.16f, 0.72f, 10, 0xFFFF787D);
        ring(0.16f, 0.72f, 0xFFFF787D, 0.5f);
    }
    static void villagerFinished() { COMBO.add(1); }
    static void comboStep(int step) {
        shownCombo = step;
        comboAge = 0;
        var sounds = Minecraft.getInstance().getSoundManager();
        if (comboClip != null) sounds.stop(comboClip);
        comboClip = new Clip("combo_" + shownCombo, false);
        CLIPS.add(comboClip);
        sounds.play(comboClip);
        if (shownCombo == 8) {
            FEEDBACK.finale();
            ZOOM.trigger(true);
            burst(0.5f, 0.2f, 64, 0xFFFFE59E);
            ring(0.5f, 0.2f, 0xFFFFB3F4, 1.6f);
            ring(0.5f, 0.2f, 0xFF67FFD1, 1.1f);
            int count = Math.min(500, 600 - DROPS.size());
            for (int i = 0; i < count; i++) DROPS.add(new Drop());
        }
    }
    static void tick() {
        clock++;
        ZOOM.tick();
        if (cashCooldown > 0) cashCooldown--;
        lastTickNanos = System.nanoTime();
        if (reactionPulse > 0) reactionPulse--;
        FEEDBACK.tick();
        displayedEarnings = HudMotion.counterStep(displayedEarnings, EARNINGS.total());
        SPARKS.removeIf(p -> ++p.age >= p.life);
        RINGS.removeIf(r -> ++r.age >= 32);
        comboAge++;
        if (earningsPulse > 0) earningsPulse--;
        if (flybyCooldown > 0) flybyCooldown--;
        if (pendingFlybys > 0 && flybyCooldown == 0
                && (jackpotClip == null || !Minecraft.getInstance().getSoundManager().isActive(jackpotClip))) {
            pendingFlybys--;
            flybyCooldown = 144; // Original jackpot recording: 7.131 s, never overlap.
            FLYBYS.add(new Flyby());
            FEEDBACK.sold(true);
            reactionPulse = 20;
            reactions++;
            ZOOM.trigger(false);
            burst(0.16f, 0.72f, 44, 0xFFFF91D9);
            ring(0.16f, 0.72f, 0xFF73FFD1, 1.2f);
            jackpotClip = new Clip("slotmachinejackpot", false);
            CLIPS.add(jackpotClip);
            Minecraft.getInstance().getSoundManager().play(jackpotClip);
        }
        FLYBYS.removeIf(f -> ++f.age > FlybyMotion.LIFETIME);
        int step = COMBO.tick();
        if (step != 0) comboStep(step);
        TOASTS.removeIf(t -> ++t.age > 65);
        DROPS.removeIf(d -> ++d.age > d.life);
        var soundManager = Minecraft.getInstance().getSoundManager();
        CLIPS.removeIf(c -> !soundManager.isActive(c));
        if (music != null) music.gain((comboClip != null && soundManager.isActive(comboClip))
                || (jackpotClip != null && soundManager.isActive(jackpotClip)) ? 0.2f : 0.55f);
    }
    static void clear() {
        FEEDBACK.clear();
        ZOOM.clear();
        reactions = cashCooldown = 0;
        comboClip = jackpotClip = null;
        displayedEarnings = 0;
        reactionPulse = 0;
        lastTickNanos = 0;
        clock = 0;
        SPARKS.clear();
        RINGS.clear();
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
        float time = visualStart == 0 ? 0 : (System.nanoTime() - visualStart) / 1_000_000_000f;
        float scale = HudMotion.hudScale(w, h);
        float partial = Math.min(1f, (System.nanoTime() - lastTickNanos) / 50_000_000f);
        for (Ring r : RINGS) HypeHud.ring(g, r.x * w, r.y * h, (r.age + partial) / 32f, r.color, r.size * scale);
        for (Spark p : SPARKS) {
            float t = p.age + partial;
            float x = p.x * w + p.vx * t * scale;
            float y = p.y * h + (p.vy * t + 0.035f * t * t) * scale;
            float fade = 1 - t / p.life;
            HypeHud.star(g, x, y, (1 + fade) * scale, HudMotion.tint(p.color, fade * 0.7f));
        }
        HypeHud.portrait(g, FACES.get(PortraitArt.name(FEEDBACK.emotion(), clock, reactions)), FEEDBACK.emotion(), clock, time, shownCombo, reactionPulse);
        for (int i = 0; i < Math.min(5, TOASTS.size()); i++) {
            Toast t = TOASTS.get(TOASTS.size() - 1 - i);
            HypeHud.toast(g, EMERALD, t.amount, t.age + partial, i);
        }
        HypeHud.wallet(g, EMERALD, EARNINGS.total(), displayedEarnings, earningsPulse, time);
        HypeHud.combo(g, shownCombo, comboAge + partial, time);
        for (Flyby f : FLYBYS) {
            float progress = FlybyMotion.progress(f.age);
            float x = FlybyMotion.x(f.age, w);
            float y = (float) (h * f.height + Math.sin(progress * Math.PI * 2) * h * 0.12);
            for (int i = 1; i <= 7; i++) {
                HypeHud.star(g, x - i * 10, y + (float) Math.sin(time * 8 - i) * 9,
                        2.5f - i * 0.2f, HudMotion.tint(0x73FFD5, (8 - i) / 24f));
            }
            // Only a normal HUD texture: no Villager, world, entity ID, render
            // state extraction, InventoryScreen or mob-renderer mixins involved.
            g.pose().pushMatrix();
            try {
                g.pose().translate(x, y);
                g.pose().rotate(FlybyMotion.angle(f.age));
                float spriteScale = 0.58f * HudMotion.hudScale(w, h);
                g.pose().scale(spriteScale, spriteScale);
                g.blit(RenderPipelines.GUI_TEXTURED, FLYBY_FRAMES[(f.age / 3) % 8], -64, -128, 0, 0, 128, 256, 128, 256);
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
            g.blit(RenderPipelines.GUI_TEXTURED, EMERALD, -8, -8, 0, 0, 16, 16, 16, 16);
            g.pose().popMatrix();
        }
    }
    private static void burst(float x, float y, int count, int color) {
        for (int i = 0; i < count; i++) {
            if (SPARKS.size() == MAX_SPARKS) SPARKS.remove(0);
            SPARKS.add(new Spark(x, y, color));
        }
    }
    private static void ring(float x, float y, int color, float size) {
        if (RINGS.size() == MAX_RINGS) RINGS.remove(0);
        RINGS.add(new Ring(x, y, color, size));
    }
    private static final class Spark {
        final float x, y, vx, vy;
        final int color, life;
        int age;
        Spark(float x, float y, int color) {
            this.x = x; this.y = y; this.color = color;
            double angle = RNG.nextDouble() * Math.PI * 2;
            float speed = 0.6f + RNG.nextFloat() * 2.3f;
            vx = (float) Math.cos(angle) * speed;
            vy = (float) Math.sin(angle) * speed - 0.8f;
            life = 25 + RNG.nextInt(18);
        }
    }
    private static final class Ring {
        final float x, y, size;
        final int color;
        int age;
        Ring(float x, float y, int color, float size) {
            this.x = x; this.y = y; this.color = color; this.size = size;
        }
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
