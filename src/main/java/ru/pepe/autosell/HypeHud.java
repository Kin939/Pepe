package ru.pepe.autosell;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;

/** Emerald arcade HUD. Only 2D extraction; never constructs entities or touches GL. */
final class HypeHud {
    private static final int INK = 0xF0141728, MINT = 0xFF80FFD0, MUTED = 0xFF71948F;
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
        g.fill(x + 5, y + h - 1, x + w - 5, y + h, 0xFFAA598C);
    }
    private static final Identifier CRT = Identifier.fromNamespaceAndPath(Pepe.ID, "textures/gui/hype_crt.png");
    static void portrait(GuiGraphicsExtractor g, Identifier face, SaleFeedback.Emotion mood,
                         long clock, float time, int combo, int pulse) {
        float scale = HudMotion.hudScale(g.guiWidth(), g.guiHeight());
        int accent = mood == SaleFeedback.Emotion.FINALE ? 0xFFFF91D9 : mood.color;
        g.pose().pushMatrix();
        try {
            float intro = HudMotion.easeOut(time / 0.45f);
            g.pose().translate(8 - (1-intro)*140, g.guiHeight() - 33 - 154 * scale);
            g.pose().scale(scale, scale);
            float kick = pulse / 20f;
            g.pose().translate((float)Math.sin(time*40)*kick*1.5f, (float)Math.sin(time*3)*0.7f);
            g.fill(18, 35, 111, 117, 0xFF092526);
            g.pose().pushMatrix();
            try {
                g.pose().translate(64, 76 + (float)Math.sin(time*2.5));
                g.pose().scale(0.65f, 0.65f);
                g.blit(RenderPipelines.GUI_TEXTURED, face, -64, -64, 0, 0, 128, 128, 128, 128);
            } finally { g.pose().popMatrix(); }
            for (int row=39; row<115; row+=3) g.fill(21,row,107,row+1,0x10020B0C);
            int sweep=39+(int)(time*15%74);
            g.fill(21,sweep,107,sweep+1,0x146CFFD8);
            g.blit(RenderPipelines.GUI_TEXTURED, CRT, 0, 0, 0, 0, 128, 154, 128, 154);
            g.pose().pushMatrix();
            try {
                g.pose().translate(64, 23);
                g.pose().scale(0.76f,0.76f);
                centered(g,"HYPE TV",0,0,0xFFFFBBE7);
            } finally { g.pose().popMatrix(); }
            for (int i=0;i<8;i++) g.fill(21+i*11,121,28+i*11,123,i<combo ? accent : 0xFF243F40);
            g.fill(109,26,112,29,(clock/18%2==0)?0xFF75FFD4:0xFF275C4E);
            g.pose().pushMatrix();
            try {
                g.pose().translate(67,132);
                g.pose().scale(0.52f,0.52f);
                centered(g,text("face."+mood.texture),0,0,accent);
            } finally { g.pose().popMatrix(); }
        } finally { g.pose().popMatrix(); }
    }
    static void wallet(GuiGraphicsExtractor g, Identifier emerald, long total, double displayed, int pulse, float time) {
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
                g.blit(RenderPipelines.GUI_TEXTURED, emerald, 0, 0, 0, 0, 16, 16, 16, 16);
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
        ArcadeType.combo(g, step, age, time);
    }
    static void toast(GuiGraphicsExtractor g, Identifier emerald, int amount, float age, int index) {
        float progress = HudMotion.easeOut(age / 9f);
        float opacity = Math.min(1, (65 - age) / 18f);
        if (opacity <= 0) return;
        String value = "+" + amount;
        int width = Minecraft.getInstance().font.width(value) + 34;
        int x = g.guiWidth() - width - 12 + Math.round((1 - progress) * (width + 14));
        int y = 14 + index * 25;
        g.fill(x, y, x + width, y + 20, HudMotion.tint(0x0C211F, opacity * 0.9f));
        g.fill(x, y + 1, x + 2, y + 19, HudMotion.tint(MINT, opacity));
        g.blit(RenderPipelines.GUI_TEXTURED, emerald, x + 5, y + 2, 0, 0, 16, 16, 16, 16);
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
