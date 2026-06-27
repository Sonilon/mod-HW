package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

import java.util.List;

public class TriggerListScreen extends Screen {

    private final Screen parent;
    private static final int BG          = 0xE0101820;
    private static final int ROW_BG      = 0xCC1A2535;
    private static final int ROW_HOVER   = 0xCC243545;
    private static final int ACCENT      = 0xFF00E676;
    private static final int ACCENT_RED  = 0xFFFF5252;
    private static final int TEXT        = 0xFFE0E0E0;
    private static final int DIM         = 0xFF90A4AE;

    private int scrollOffset = 0;
    private static final int ROW_H = 38;
    private static final int LIST_TOP = 55;

    public TriggerListScreen(Screen parent) {
        super(Text.literal("§e⚡ Триггеры ChatGuard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Кнопка добавить
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§a+ Добавить триггер"),
                btn -> client.setScreen(new TriggerEditScreen(this, null, -1))
        ).dimensions(width / 2 - 80, height - 30, 160, 20).build());

        // Назад
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§7← Назад"),
                btn -> client.setScreen(parent)
        ).dimensions(6, height - 30, 80, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, BG);
        ctx.fill(0, 0, width, 2, ACCENT);

        // Заголовок
        ctx.fill(0, 5, width, 45, 0xFF1A2A3A);
        ctx.fill(0, 5, 4, 45, 0xFFFFD740);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§e§l⚡ Триггеры"), width / 2, 14, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7Слово — Время — Правило — Причина"), width / 2, 27, DIM);

        List<ChatGuardConfig.Trigger> triggers = ChatGuardConfig.getInstance().triggers;

        if (triggers.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§7Триггеры не добавлены. Нажмите «+ Добавить триггер»"),
                    width / 2, LIST_TOP + 20, DIM);
        }

        int y = LIST_TOP - scrollOffset;
        for (int i = 0; i < triggers.size(); i++) {
            ChatGuardConfig.Trigger t = triggers.get(i);
            if (y + ROW_H < LIST_TOP || y > height - 40) { y += ROW_H + 4; continue; }

            boolean hover = mx >= 4 && mx <= width - 4 && my >= y && my <= y + ROW_H;
            ctx.fill(4, y, width - 4, y + ROW_H, hover ? ROW_HOVER : ROW_BG);
            ctx.fill(4, y, 6, y + ROW_H, t.enabled ? ACCENT : 0xFF607D8B);

            // Статус
            String status = t.enabled ? "§a●" : "§7○";
            ctx.drawTextWithShadow(textRenderer, Text.literal(status), 10, y + 4, 0xFFFFFFFF);

            // Слово
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal("§e" + t.word), 22, y + 4, TEXT);
            // Время и правило
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal("§7Время: §f" + t.time + "  §7Правило: §f" + t.rule),
                    22, y + 15, TEXT);
            // Причина
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal("§8" + t.reason), 22, y + 26, DIM);

            // Кнопки: редактировать / удалить
            final int idx = i;
            // Inline buttons drawn — we'll place real buttons per row in init
            y += ROW_H + 4;
        }

        ctx.fill(0, height - 2, width, height, ACCENT);
        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        List<ChatGuardConfig.Trigger> triggers = ChatGuardConfig.getInstance().triggers;
        int y = LIST_TOP - scrollOffset;
        for (int i = 0; i < triggers.size(); i++) {
            if (my >= y && my <= y + ROW_H && mx >= 4 && mx <= width - 4) {
                if (button == 0) {
                    // Левый клик — редактировать
                    final int idx = i;
                    client.setScreen(new TriggerEditScreen(this, triggers.get(idx), idx));
                    return true;
                } else if (button == 1) {
                    // Правый клик — удалить
                    triggers.remove(i);
                    ChatGuardConfig.save();
                    return true;
                }
            }
            y += ROW_H + 4;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        int total = ChatGuardConfig.getInstance().triggers.size() * (ROW_H + 4);
        int maxScroll = Math.max(0, total - (height - LIST_TOP - 40));
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - amount * 10));
        return true;
    }

    @Override
    public boolean shouldPause() { return false; }
}
