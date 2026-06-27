package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

public class ChatGuardSettingsScreen extends Screen {

    private final Screen parent;
    private static final int BG_COLOR     = 0xE0101820;
    private static final int HEADER_COLOR = 0xFF1A2A3A;
    private static final int ACCENT       = 0xFF00E676;
    private static final int ACCENT2      = 0xFFFFD740;
    private static final int TEXT_COLOR   = 0xFFE0E0E0;
    private static final int DIM_TEXT     = 0xFF90A4AE;

    public ChatGuardSettingsScreen(Screen parent) {
        super(Text.literal("§a§lChatGuard §r§7— Настройки"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int by = 60;
        int bw = 200;
        int bh = 20;
        int gap = 26;

        // Кнопка — Триггеры
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§e⚡ Управление триггерами"),
                btn -> client.setScreen(new TriggerListScreen(this))
        ).dimensions(cx - bw / 2, by, bw, bh).build());

        // Кнопка — Звук вкл/выкл
        addDrawableChild(ButtonWidget.builder(
                soundLabel(),
                btn -> {
                    ChatGuardConfig.getInstance().soundEnabled = !ChatGuardConfig.getInstance().soundEnabled;
                    ChatGuardConfig.save();
                    btn.setMessage(soundLabel());
                }
        ).dimensions(cx - bw / 2, by + gap, bw, bh).build());

        // Кнопка — Формат команды мута
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§b✎ Формат команды мута"),
                btn -> client.setScreen(new MuteFormatScreen(this))
        ).dimensions(cx - bw / 2, by + gap * 2, bw, bh).build());

        // Кнопка — Формат сообщений
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§d✎ Формат сообщений"),
                btn -> client.setScreen(new MessageFormatScreen(this))
        ).dimensions(cx - bw / 2, by + gap * 3, bw, bh).build());

        // Кнопка — Регекс ника
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§7⚙ Паттерн ника (regex)"),
                btn -> client.setScreen(new NickRegexScreen(this))
        ).dimensions(cx - bw / 2, by + gap * 4, bw, bh).build());

        // Закрыть
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§c✖ Закрыть"),
                btn -> client.setScreen(parent)
        ).dimensions(cx - 50, by + gap * 5 + 10, 100, bh).build());
    }

    private Text soundLabel() {
        boolean on = ChatGuardConfig.getInstance().soundEnabled;
        return Text.literal(on ? "§a🔊 Звук: ВКЛ" : "§7🔇 Звук: ВЫКЛ");
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        // Фон
        ctx.fill(0, 0, width, height, BG_COLOR);

        // Верхняя полоска
        ctx.fill(0, 0, width, 2, ACCENT);

        // Заголовочный блок
        ctx.fill(0, 5, width, 45, HEADER_COLOR);
        ctx.fill(0, 5, 4, 45, ACCENT);

        // Заголовок
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§a§lChat§f§lGuard"), width / 2, 14, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7Помощник модератора"), width / 2, 27, DIM_TEXT);

        // Нижняя полоска
        ctx.fill(0, height - 2, width, height, ACCENT);

        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
