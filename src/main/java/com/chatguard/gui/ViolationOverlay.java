package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ViolationOverlay {

    private static final List<ViolationAlert> alerts = new ArrayList<>();
    private static final int MAX_ALERTS = 5;
    private static final int ALERT_DURATION = 6000; // ms

    public static void addAlert(String nick, String word, ChatGuardConfig.Trigger trigger) {
        if (alerts.size() >= MAX_ALERTS) alerts.remove(0);
        alerts.add(new ViolationAlert(nick, word, trigger, System.currentTimeMillis()));
    }

    public static void render(DrawContext ctx, int screenWidth, int screenHeight, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) return;

        long now = System.currentTimeMillis();
        // Удалить истёкшие
        alerts.removeIf(a -> now - a.startTime > ALERT_DURATION);

        int y = 10;
        for (ViolationAlert alert : alerts) {
            long elapsed = now - alert.startTime;
            float progress = Math.min(1f, elapsed / 300f); // fade-in 300ms
            float fadeOut  = elapsed > ALERT_DURATION - 600
                    ? Math.max(0f, (ALERT_DURATION - elapsed) / 600f) : 1f;
            int alpha = (int)(Math.min(progress, fadeOut) * 220);

            renderAlert(ctx, alert, screenWidth, y, alpha);
            y += 58;
        }
    }

    private static void renderAlert(DrawContext ctx, ViolationAlert a, int sw, int y, int alpha) {
        if (alpha <= 10) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        int w = 260;
        int h = 50;
        int x = sw - w - 8;

        int bg    = (alpha << 24) | 0x0D1B26;
        int bgAccent = (alpha << 24) | 0x1A2535;
        int red   = (alpha << 24) | 0xB71C1C;
        int redBr = (alpha << 24) | 0xFF5252;
        int white = (alpha << 24) | 0xE0E0E0;
        int dim   = (alpha << 24) | 0x90A4AE;
        int yellow= (alpha << 24) | 0xFFD740;

        // Фон
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.fill(x, y, x + w, y + h / 2, bgAccent); // верхняя полоска чуть светлее

        // Левая красная полоска
        ctx.fill(x, y, x + 4, y + h, red);
        ctx.fill(x, y, x + 4, y + 5, redBr); // яркий акцент сверху

        // Рамка
        ctx.fill(x, y, x + w, y + 1, red);         // top
        ctx.fill(x, y + h - 1, x + w, y + h, red); // bottom
        ctx.fill(x + w - 1, y, x + w, y + h, red); // right

        // Иконка предупреждения
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§c⚠"), x + 8, y + 5, white);

        // Заголовок
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§c§lНАРУШИТЕЛЬ"), x + 22, y + 5, (alpha << 24) | 0xFF5252);

        // Ник
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§f" + a.nick), x + 8, y + 17, white);

        // Слово-триггер
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§7«§e" + a.word + "§7»"), x + 8, y + 28, white);

        // Правило и время
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§7Правило §a" + a.trigger.rule
                        + " §7| §fМут: §e" + a.trigger.time),
                x + 8, y + 39, dim);

        // Подсказка
        ctx.drawTextWithShadow(mc.textRenderer,
                net.minecraft.text.Text.literal("§7[Нажмите на сообщение в чате]"),
                x + w - 140, y + 39, (alpha << 24) | 0x607D8B);
    }

    public static class ViolationAlert {
        public final String nick;
        public final String word;
        public final ChatGuardConfig.Trigger trigger;
        public final long startTime;

        public ViolationAlert(String nick, String word, ChatGuardConfig.Trigger trigger, long startTime) {
            this.nick      = nick;
            this.word      = word;
            this.trigger   = trigger;
            this.startTime = startTime;
        }
    }
}
