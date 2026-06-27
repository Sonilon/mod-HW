package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

public class MessageFormatScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget prefixField;
    private TextFieldWidget formatField;

    public MessageFormatScreen(Screen parent) {
        super(Text.literal("§d✎ Формат сообщений"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        ChatGuardConfig cfg = ChatGuardConfig.getInstance();

        prefixField = new TextFieldWidget(textRenderer, cx - 150, 70, 300, 20,
                Text.literal("Префикс оповещения"));
        prefixField.setText(cfg.alertPrefix);
        prefixField.setMaxLength(128);
        addDrawableChild(prefixField);

        formatField = new TextFieldWidget(textRenderer, cx - 150, 110, 300, 20,
                Text.literal("Формат оповещения"));
        formatField.setText(cfg.violationFormat);
        formatField.setMaxLength(256);
        addDrawableChild(formatField);

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§a✔ Сохранить"),
                btn -> {
                    cfg.alertPrefix      = prefixField.getText();
                    cfg.violationFormat  = formatField.getText();
                    ChatGuardConfig.save();
                    client.setScreen(parent);
                }
        ).dimensions(cx - 55, 140, 110, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§7← Назад"),
                btn -> client.setScreen(parent)
        ).dimensions(cx - 55, 165, 110, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0xE0101820);
        ctx.fill(0, 0, width, 2, 0xFF00E676);
        ctx.fill(0, 5, width, 45, 0xFF1A2A3A);
        ctx.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, 16, 0xFFFFFFFF);

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7Префикс (строка над оповещением):"),
                width / 2, 57, 0xFF90A4AE);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7Формат — §e{nick} {word} {time} {reason}"),
                width / 2, 97, 0xFF90A4AE);

        ctx.fill(0, height - 2, width, height, 0xFF00E676);
        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
