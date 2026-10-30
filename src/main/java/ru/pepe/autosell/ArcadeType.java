package ru.pepe.autosell;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Independent pixel glyphs: hand-drawn bevels, pink/emerald animated specular bands. */
final class ArcadeType {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Identifier[][] GLYPHS = new Identifier[36][6];
    private static final String[] RANKS = {"NICE","SWEET","WILD","HYPED","SUPER","INSANE","MEGA","JACKPOT"};
    static {
        for (int i = 0; i < ALPHABET.length(); i++) for (int phase = 0; phase < 6; phase++)
            GLYPHS[i][phase] = Identifier.fromNamespaceAndPath(Pepe.ID, "textures/gui/type/" + Character.toLowerCase(ALPHABET.charAt(i)) + "_" + phase + ".png");
    }
    static void word(GuiGraphicsExtractor g, String value, float x, float y, float scale, float time, float energy) {
        g.pose().pushMatrix();
        try {
            g.pose().translate(x, y);
            g.pose().scale(scale, scale);
            float width = value.length() * 25 - 1;
            for (int i = 0; i < value.length(); i++) {
                int index = ALPHABET.indexOf(value.charAt(i));
                if (index < 0) continue;
                g.pose().pushMatrix();
                try {
                    float bob = (float)Math.sin(time * 5 + i * 0.7) * energy * 2.4f;
                    g.pose().translate(-width / 2 + i * 25, bob);
                    g.pose().rotate((float)Math.sin(time * 3 + i) * 0.015f * energy);
                    int phase = Math.floorMod((int)(time * 11) + i, 6);
                    g.blit(RenderPipelines.GUI_TEXTURED, GLYPHS[index][phase], 0, 0, 0, 0, 32, 48, 32, 48);
                } finally { g.pose().popMatrix(); }
            }
        } finally { g.pose().popMatrix(); }
    }
    static void combo(GuiGraphicsExtractor g, int step, float age, float time) {
        int life = step == 8 ? 115 : 80;
        if (step < 1 || step > 8 || age >= life) return;
        float enter = HudMotion.easeOut(age / 10);
        float exit = HudMotion.clamp((age - (life - 15)) / 15);
        float base = Math.max(0.65f, HudMotion.hudScale(g.guiWidth(), g.guiHeight()));
        float energy = step / 8f;
        float slide = (1 - enter) * 70;
        float dx = switch (step) { case 1, 4 -> -slide; case 2, 5 -> slide; default -> 0; };
        float dy = switch (step) { case 3, 6 -> -slide * 0.5f; case 7 -> (float)Math.sin(age * 1.2) * (1-enter)*9; default -> slide * 0.15f; };
        float spring = (float)Math.sin(age * 0.55) * (float)Math.exp(-age / 9) * (0.1f + energy * 0.12f);
        float scale = base * (0.8f + energy * 0.22f) * Math.max(0.02f, 1 - exit) * (1 + spring);
        g.pose().pushMatrix();
        try {
            // Above/below the crosshair stays clear; no card, panel or full-width background.
            g.pose().translate(g.guiWidth() / 2f + dx, g.guiHeight() / 2f + 22 + dy - exit * 12);
            g.pose().scale(scale, scale);
            g.pose().rotate((step % 2 == 0 ? -0.055f : 0.035f) * (1-exit));
            if (step >= 4) {
                for (int i = 0; i < step + 3; i++) {
                    float a = (float)(i * Math.PI * 2 / (step + 3)) + time * 0.3f;
                    HypeHud.star(g, (float)Math.cos(a) * 100, 22 + (float)Math.sin(a) * 34,
                            1.5f + energy, HudMotion.tint(i % 2 == 0 ? 0xFF8AD8 : 0x73FFD1, 0.6f * (1-exit)));
                }
            }
            word(g, "COMBO", -21, 0, 0.68f, time, energy);
            word(g, Integer.toString(step), 62, -14, 1.3f + energy * 0.25f, time + 0.2f, energy);
            word(g, RANKS[step-1], 2, 36, step == 8 ? 0.42f : 0.32f, time + 0.4f, energy);
            if (step == 8) {
                for (int i = 0; i < 8; i++) {
                    g.pose().pushMatrix();
                    try {
                        g.pose().translate(0, 22);
                        g.pose().rotate((float)(Math.PI * 2 * i / 8) + time * 0.1f);
                        g.fill(95, -1, 119 + i % 3 * 5, 1, HudMotion.tint(i%2==0 ? 0xFF8AD8 : 0x73FFD1, 0.25f));
                    } finally { g.pose().popMatrix(); }
                }
            }
        } finally { g.pose().popMatrix(); }
    }
}
