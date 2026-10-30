package ru.pepe.autosell;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Emerald arcade HUD. Only 2D extraction; never constructs entities or touches GL. */
final class HypeHud {
    private static final int INK = 0xF009161B, MINT = 0xFF64FFD0, MUTED = 0xFF71948F;
    private static String text(String key) { return Component.translatable("hud.pepe." + key).getString(); }
    private static void label(GuiGraphicsExtractor g, String text, int x, int y, int color) {
        g.text(Minecraft.getInstance().font, text, x, y, color, false);
    }
    private static void centered(GuiGraphicsExtractor g, String text, int x, int y, int color) {
        label(g, text, x - Minecraft.getInstance().font.width(text) / 2, y, color);
    }
    private static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h, int accent) {
        g.fill(x + 3, y + 4, x + w + 3, y + h + 4, 0x60000000);
        g.fill(x - 2, y + 4, x + w + 2, y + h - 4, HudMotion.tint(accent, 0.07f));
        g.fill(x, y + 2, x + w, y + h - 2, INK);
        g.fill(x + 2, y, x + w - 2, y + h, INK);
        g.fill(x + 3, y, x + w - 3, y + 1, HudMotion.tint(accent, 0.8f));
        g.fill(x, y + 3, x + 1, y + 11, accent);
        g.fill(x + w - 1, y + h - 11, x + w, y + h - 3, accent);
        g.fill(x + 5, y + h - 1, x + w - 5, y + h, 0xFF1D393A);
    }
    static void portrait(GuiGraphicsExtractor g, Identifier face, SaleFeedback.Emotion mood,
                         long clock, float time, int combo, int pulse) {
        float scale = HudMotion.hudScale(g.guiWidth(), g.guiHeight());
        int accent = mood == SaleFeedback.Emotion.FINALE ? rainbow(time * 0.12f) : mood.color;
        g.pose().pushMatrix();
        try {
            g.pose().translate(10 - (1 - HudMotion.easeOut(time / 0.45f)) * 125, g.guiHeight() - 36 - 128 * scale);
            g.pose().scale(scale, scale);
            float kick = pulse / 20f;
            if (mood == SaleFeedback.Emotion.FAILURE) g.pose().translate((float) Math.sin(time * 55) * kick * 2, 0);
            panel(g, 0, 0, 112, 128, accent);
            label(g, "HYPE TV", 10, 8, 0xFFE0FFF3);
            g.fill(90, 9, 94, 13, (clock / 18 % 2 == 0) ? MINT : 0xFF296F54);
            g.fill(98, 10, 103, 12, 0xFF436D63);
            g.fill(8, 23, 104, 100, 0xFF020C0D);
            g.fill(10, 25, 102, 98, 0xFF102C29);
            // Portrait breathes and reacts within a generously inset screen.
            g.pose().pushMatrix();
            try {
                float bob = (float) Math.sin(time * 2.5) * 1.2f;
                float stretch = 1 + (float) Math.sin(time * 12) * kick * 0.035f;
                g.pose().translate(56, 62 + bob);
                g.pose().scale(stretch, 2 - stretch);
                g.blit(RenderPipelines.GUI_TEXTURED, face, -32, -32, 0, 0, 64, 64, 64, 64);
                if (mood == SaleFeedback.Emotion.IDLE && clock % 115 < 4) {
                    g.fill(-15, -4, -4, 2, 0xFFD2AE83);
                    g.fill(4, -4, 15, 2, 0xFFD2AE83);
                    g.fill(-15, 0, -4, 2, 0xFF524432);
                    g.fill(4, 0, 15, 2, 0xFF524432);
                }
            } finally { g.pose().popMatrix(); }
            for (int row = 27; row < 98; row += 3) g.fill(10, row, 102, row + 1, 0x14020B0C);
            int sweep = 27 + (int) (time * 16 % 69);
            g.fill(10, sweep, 102, sweep + 1, 0x126CFFD8);
            g.fill(11, 26, 13, 95, 0x2256FFBF);
            g.fill(14, 26, 44, 27, 0x3066FFD2);
            // Eight LEDs mirror actual combo steps, never increment the combo.
            for (int i = 0; i < 8; i++) g.fill(12 + i * 11, 103, 20 + i * 11, 105,
                    i < combo ? accent : 0xFF203C3D);
            String emotion = text("face." + mood.texture);
            g.pose().pushMatrix();
            try {
                g.pose().translate(53, 113);
                g.pose().scale(0.7f, 0.7f);
                centered(g, emotion, 0, 0, accent);
            } finally { g.pose().popMatrix(); }
            for (int i = 0; i < 3; i++) {
                int height = 2 + (int) ((Math.sin(time * 5 + i * 1.8) + 1) * 3);
                g.fill(94 + i * 3, 122 - height, 96 + i * 3, 122, HudMotion.tint(accent, 0.6f));
            }
        } finally { g.pose().popMatrix(); }
    }
    static void wallet(GuiGraphicsExtractor g, ItemStack emerald, long total, double displayed, int pulse, float time) {
        float scale = HudMotion.hudScale(g.guiWidth(), g.guiHeight());
        int width = Math.max(152, Math.round(Minecraft.getInstance().font.width(Long.toString(total)) * 1.7f) + 60);
        g.pose().pushMatrix();
        try {
            g.pose().translate(g.guiWidth() - 10 - width * scale, g.guiHeight() - 12 - 71 * scale);
            g.pose().scale(scale, scale);
            panel(g, 0, 0, width, 71, MINT);
            label(g, text("session"), 10, 8, MUTED);
            g.pose().pushMatrix();
            try {
                g.pose().translate(10, 23);
                g.pose().scale(1.7f, 1.7f);
                label(g, Long.toString(Math.round(displayed)), 0, 0, pulse > 0 ? 0xFFBFFFE5 : 0xFFF2FFF9);
            } finally { g.pose().popMatrix(); }
            g.pose().pushMatrix();
            try {
                g.pose().translate(width - 29, 24 - (float) Math.sin(time * 3) * 1.5f);
                g.pose().scale(1.25f, 1.25f);
                g.item(emerald, 0, 0);
            } finally { g.pose().popMatrix(); }
            int progress = (int) (total % 64);
            g.fill(10, 47, width - 10, 50, 0xFF183738);
            int filled = Math.round((width - 20) * progress / 64f);
            if (filled > 0) {
                g.fill(10, 47, 10 + filled, 50, 0xFF37C798);
                g.fill(9 + filled, 47, 10 + filled, 50, 0xFFCAFFEA);
            }
            g.pose().pushMatrix();
            try {
                g.pose().translate(10, 56);
                g.pose().scale(0.65f, 0.65f);
                label(g, text("next_stack"), 0, 0, MUTED);
            } finally { g.pose().popMatrix(); }
            String fraction = progress + "/64";
            label(g, fraction, width - 10 - Minecraft.getInstance().font.width(fraction), 55, 0xFFA6D7C6);
        } finally { g.pose().popMatrix(); }
    }
    static void combo(GuiGraphicsExtractor g, int step, float age, float time) {
        if (step == 0 || age >= 90) return;
        float fade = age < 65 ? 1 : HudMotion.clamp((90 - age) / 25f);
        float intro = HudMotion.easeOut(age / 8f);
        int x = g.guiWidth() / 2, y = 20 - Math.round((1 - intro) * 18);
        boolean finale = step == 8;
        int accent = finale ? rainbow(time * 0.15f) : 0xFF6DFFD0;
        g.pose().pushMatrix();
        try {
            g.pose().translate(x, y);
            float scale = 0.9f + intro * 0.1f;
            g.pose().scale(scale, scale);
            g.fill(-65, 0, 65, 39, HudMotion.tint(0x06151A, fade * 0.9f));
            g.fill(-65, 0, -63, 39, HudMotion.tint(accent, fade));
            g.fill(63, 0, 65, 39, HudMotion.tint(accent, fade));
            centered(g, finale ? "HYPE OVERLOAD" : "HYPE COMBO", 0, 6, HudMotion.tint(accent, fade));
            String number = Integer.toString(step);
            g.pose().pushMatrix();
            try {
                g.pose().translate(-40, 19);
                float pop = 1.6f + (float) Math.exp(-age / 8f) * 0.65f;
                g.pose().scale(pop, pop);
                centered(g, number, 0, 0, HudMotion.tint(0xF1FFF7, fade));
            } finally { g.pose().popMatrix(); }
            for (int i = 0; i < 8; i++) g.fill(-19 + i * 9, 24, -12 + i * 9, 29,
                    HudMotion.tint(i < step ? accent : 0x25403F, fade));
            if (finale) {
                for (int i = 0; i < 12; i++) {
                    float a = i * (float) Math.PI / 6 + time * 0.25f;
                    star(g, (float) Math.cos(a) * 88, 20 + (float) Math.sin(a) * 36,
                            1.8f, HudMotion.tint(rainbow(i / 12f + time * 0.1f), fade * 0.7f));
                }
            }
        } finally { g.pose().popMatrix(); }
    }
    static void toast(GuiGraphicsExtractor g, ItemStack emerald, int amount, float age, int index) {
        float progress = HudMotion.easeOut(age / 9f);
        float opacity = Math.min(1, (65 - age) / 18f);
        if (opacity <= 0) return;
        String value = "+" + amount;
        int width = Minecraft.getInstance().font.width(value) + 34;
        int x = g.guiWidth() - width - 12 + Math.round((1 - progress) * (width + 14));
        int y = 14 + index * 25;
        g.fill(x, y, x + width, y + 20, HudMotion.tint(0x0C211F, opacity * 0.9f));
        g.fill(x, y + 1, x + 2, y + 19, HudMotion.tint(MINT, opacity));
        g.item(emerald, x + 5, y + 2);
        label(g, value, x + 25, y + 6, HudMotion.tint(0xC8FFE3, opacity));
    }
    static void star(GuiGraphicsExtractor g, float x, float y, float size, int color) {
        g.pose().pushMatrix();
        try {
            g.pose().translate(x, y);
            g.pose().rotate((float) Math.PI / 4);
            int s = Math.max(1, Math.round(size));
            g.fill(-s - 1, -s - 1, s + 1, s + 1, HudMotion.tint(color, ((color >>> 24) / 255f) * 0.12f));
            g.fill(-s, -1, s + 1, 1, color);
            g.fill(-1, -s, 1, s + 1, color);
        } finally { g.pose().popMatrix(); }
    }
    static void ring(GuiGraphicsExtractor g, float x, float y, float progress, int color, float size) {
        float radius = (8 + HudMotion.easeOut(progress) * 65) * size;
        int tint = HudMotion.tint(color, (1 - progress) * (1 - progress) * 0.32f);
        for (int i = 0; i < 32; i++) {
            g.pose().pushMatrix();
            try {
                g.pose().translate(x, y);
                g.pose().rotate((float) (Math.PI * 2 * i / 32));
                g.fill(Math.round(radius), -2, Math.round(radius) + 1, 3, tint);
            } finally { g.pose().popMatrix(); }
        }
    }
    static int rainbow(float hue) { return Color.HSBtoRGB(hue - (float) Math.floor(hue), 0.55f, 1); }
}
