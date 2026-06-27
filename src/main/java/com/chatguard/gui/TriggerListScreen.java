package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

import java.util.List;

public class TriggerListScreen extends Screen {

    private final Screen parent;
    private int scrollOffset = 0;
    private static final int ROW_H   = 34;
    private static final int LIST_TOP = 55;

    public TriggerListScreen(Screen parent) {
        super(Text.literal("§e⚡ Триггеры ChatGuard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§a+ Добавить триггер"),
                btn -> client.setScreen(new TriggerEditScreen(this, null, -1))
        ).dimensions(width / 2 - 80, height - 30, 160, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§7← Назад"),
                btn -> client.setScreen(parent)
        ).dimensions(6, height - 30, 80, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0xE0101820);
        ctx.fill(0, 0, width, 2, 0xFF00E676);
        ctx.fill(0, 5, width, 45, 0xFF1A2A3A);
        ctx.fill(0, 5, 4, 45, 0xFFFFD740);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§e§l⚡ Триггеры"), width / 2, 14, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7Слово — Время — Причина  |  ЛКМ = изменить  ПКМ = удалить"),
                width / 2, 27, 0xFF90A4AE);

        List<ChatGuardConfig.Trigger> triggers = ChatGuardConfig.getInstance().triggers;

        if (triggers.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§7Триггеры не добавлены"), width / 2, LIST_TOP + 20, 0xFF90A4AE);
        }

        int y = LIST_TOP - scrollOffset;
        for (int i = 0; i < triggers.size(); i++) {
            ChatGuardConfig.Trigger t = triggers.get(i);
            if (y + ROW_H < LIST_TOP || y > height - 40) { y += ROW_H + 3; continue; }

            boolean hover = mx >= 4 && mx <= width - 4 && my >= y && my <= y + ROW_H;
            ctx.fill(4, y, width - 4, y + ROW_H, hover ? 0xCC243545 : 0xCC1A2535);
            ctx.fill(4, y, 6, y + ROW_H, t.enabled ? 0xFF00E676 : 0xFF607D8B);

            ctx.drawTextWithShadow(textRenderer, Text.literal(t.enabled ? "§a●" : "§7○"), 10, y + 4, 0xFFFFFFFF);
            ctx.drawTextWithShadow(textRenderer, Text.literal("§e" + t.word), 22, y + 4, 0xFFE0E0E0);
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal("§7Мут: §f" + t.time + "  §7Причина: §f" + t.reason),
                    22, y + 18, 0xFF90A4AE);

            y += ROW_H + 3;
        }

        ctx.fill(0, height - 2, width, height, 0xFF00E676);
        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        List<ChatGuardConfig.Trigger> triggers = ChatGuardConfig.getInstance().triggers;
        int y = LIST_TOP - scrollOffset;
        for (int i = 0; i < triggers.size(); i++) {
            if (my >= y && my <= y + ROW_H && mx >= 4 && mx <= width - 4) {
                if (button == 0) {
                    client.setScreen(new TriggerEditScreen(this, triggers.get(i), i));
                    return true;
                } else if (button == 1) {
                    triggers.remove(i);
                    ChatGuardConfig.save();
                    return true;
                }
            }
            y += ROW_H + 3;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        int total = ChatGuardConfig.getInstance().triggers.size() * (ROW_H + 3);
        int maxScroll = Math.max(0, total - (height - LIST_TOP - 40));
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - amount * 10));
        return true;
    }

    @Override public boolean shouldPause() { return false; }
}
