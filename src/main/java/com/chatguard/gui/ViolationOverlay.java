package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ViolationOverlay {

    private static final List<ViolationAlert> alerts = new ArrayList<>();
    private static final int MAX_ALERTS    = 5;
    private static final int ALERT_DURATION = 6000;

    public static void addAlert(String nick, String word, ChatGuardConfig.Trigger trigger) {
        if (alerts.size() >= MAX_ALERTS) alerts.remove(0);
        alerts.add(new ViolationAlert(nick, word, trigger, System.currentTimeMillis()));
    }

    public static void render(DrawContext ctx, int screenWidth, int screenHeight, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) return;

        long now = System.currentTimeMillis();
        alerts.removeIf(a -> now - a.startTime > ALERT_DURATION);

        int y = 10;
        for (ViolationAlert alert : alerts) {
            long elapsed  = now - alert.startTime;
            float fadeIn  = Math.min(1f, elapsed / 300f);
            float fadeOut = elapsed > ALERT_DURATION - 600 ? Math.max(0f, (ALERT_DURATION - elapsed) / 600f) : 1f;
            int alpha = (int)(Math.min(fadeIn, fadeOut) * 220);
            renderAlert(ctx, alert, screenWidth, y, alpha);
            y += 54;
        }
    }

    private static void renderAlert(DrawContext ctx, ViolationAlert a, int sw, int y, int alpha) {
        if (alpha <= 10) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        int w = 260, h = 46;
        int x = sw - w - 8;

        int bg    = (alpha << 24) | 0x0D1B26;
        int bgTop = (alpha << 24) | 0x1A2535;
        int red   = (alpha << 24) | 0xB71C1C;
        int redBr = (alpha << 24) | 0xFF5252;
        int white = (alpha << 24) | 0xE0E0E0;
        int dim   = (alpha << 24) | 0x90A4AE;

        ctx.fill(x, y, x + w, y + h, bg);
        ctx.fill(x, y, x + w, y + h / 2, bgTop);
        ctx.fill(x, y, x + 4, y + h, red);
        ctx.fill(x, y, x + 4, y + 5, redBr);
        ctx.fill(x, y, x + w, y + 1, red);
        ctx.fill(x, y + h - 1, x + w, y + h, red);
        ctx.fill(x + w - 1, y, x + w, y + h, red);

        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§c⚠"), x + 8, y + 4, white);
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§c§lНАРУШИТЕЛЬ"), x + 20, y + 4, redBr);
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§f" + a.nick + " §7— §e«" + a.word + "»"),
                x + 8, y + 16, white);
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§7Мут: §e" + a.trigger.time + "  §8" + a.trigger.reason),
                x + 8, y + 28, dim);
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§8[нажмите на сообщение в чате]"),
                x + 8, y + 37, (alpha << 24) | 0x546E7A);
    }

    public static class ViolationAlert {
        public final String nick, word;
        public final ChatGuardConfig.Trigger trigger;
        public final long startTime;

        public ViolationAlert(String nick, String word, ChatGuardConfig.Trigger trigger, long startTime) {
            this.nick = nick; this.word = word;
            this.trigger = trigger; this.startTime = startTime;
        }
    }
}
