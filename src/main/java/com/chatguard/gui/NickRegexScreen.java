package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

public class NickRegexScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget regexField;
    private TextFieldWidget testInput;
    private String testResult = "";

    public NickRegexScreen(Screen parent) {
        super(Text.literal("§7⚙ Паттерн ника"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        ChatGuardConfig cfg = ChatGuardConfig.getInstance();

        regexField = new TextFieldWidget(textRenderer, cx - 150, 70, 300, 20,
                Text.literal("Regex для ника"));
        regexField.setText(cfg.nickRegex);
        regexField.setMaxLength(256);
        addDrawableChild(regexField);

        testInput = new TextFieldWidget(textRenderer, cx - 150, 110, 300, 20,
                Text.literal("Тестовое сообщение"));
        testInput.setText("[L/G | VIP] PlayerName [Мастер]: привет");
        testInput.setMaxLength(256);
        addDrawableChild(testInput);

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§b▶ Тест"),
                btn -> {
                    try {
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile(regexField.getText());
                        java.util.regex.Matcher m = p.matcher(testInput.getText());
                        testResult = m.find() ? "§aНик: §f" + m.group(1) : "§cНик не найден";
                    } catch (Exception e) {
                        testResult = "§cОшибка регекса: " + e.getMessage();
                    }
                }
        ).dimensions(cx - 55, 138, 110, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§a✔ Сохранить"),
                btn -> {
                    cfg.nickRegex = regexField.getText();
                    ChatGuardConfig.save();
                    client.setScreen(parent);
                }
        ).dimensions(cx - 55, 165, 110, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§7← Назад"),
                btn -> client.setScreen(parent)
        ).dimensions(cx - 55, 190, 110, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0xE0101820);
        ctx.fill(0, 0, width, 2, 0xFF00E676);
        ctx.fill(0, 5, width, 45, 0xFF1A2A3A);
        ctx.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, 16, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7Группа 1 регекса должна захватывать ник игрока"),
                width / 2, 29, 0xFF90A4AE);

        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Regex паттерн:"), width / 2 - 150, 57, 0xFFCCCCCC);
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Тест сообщения:"), width / 2 - 150, 97, 0xFFCCCCCC);

        if (!testResult.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§7Результат: " + testResult), width / 2, 162, 0xFFFFFFFF);
        }

        ctx.fill(0, height - 2, width, height, 0xFF00E676);
        super.render(ctx, mx, my, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
