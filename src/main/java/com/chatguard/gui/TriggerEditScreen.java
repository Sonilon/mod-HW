package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

public class TriggerEditScreen extends Screen {

    private final Screen parent;
    private final ChatGuardConfig.Trigger trigger;
    private final int index;

    private TextFieldWidget wordField;
    private TextFieldWidget timeField;
    private TextFieldWidget reasonField;

    public TriggerEditScreen(Screen parent, ChatGuardConfig.Trigger trigger, int index) {
        super(Text.literal(trigger == null ? "§a+ Добавить триггер" : "§e✎ Редактировать триггер"));
        this.parent  = parent;
        this.trigger = trigger != null ? trigger : new ChatGuardConfig.Trigger();
        this.index   = index;
    }

    @Override
    protected void init() {
        int cx  = width / 2;
        int top = 65;
        int fw  = 240;
        int fh  = 20;
        int gap = 38;

        wordField = new TextFieldWidget(textRenderer, cx - fw / 2, top, fw, fh, Text.literal("Слово"));
        wordField.setText(trigger.word);
        wordField.setMaxLength(64);
        addDrawableChild(wordField);

        timeField = new TextFieldWidget(textRenderer, cx - fw / 2, top + gap, fw, fh, Text.literal("Время"));
        timeField.setText(trigger.time);
        timeField.setMaxLength(20);
        addDrawableChild(timeField);

        reasonField = new TextFieldWidget(textRenderer, cx - fw / 2, top + gap * 2, fw, fh, Text.literal("Причина"));
        reasonField.setText(trigger.reason);
        reasonField.setMaxLength(128);
        addDrawableChild(reasonField);

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§a✔ Сохранить"), btn -> save()
        ).dimensions(cx - 105, top + gap * 3 + 10, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§c✖ Отмена"), btn -> client.setScreen(parent)
        ).dimensions(cx + 5, top + gap * 3 + 10, 100, 20).build());
    }

    private void save() {
        trigger.word   = wordField.getText().trim();
        trigger.time   = timeField.getText().trim();
        trigger.reason = reasonField.getText().trim();
        if (trigger.word.isEmpty()) return;
        if (index == -1) ChatGuardConfig.getInstance().triggers.add(trigger);
        ChatGuardConfig.save();
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0xE0101820);
        ctx.fill(0, 0, width, 2, 0xFF00E676);
        ctx.fill(0, 5, width, 45, 0xFF1A2A3A);
        ctx.fill(0, 5, 4, 45, 0xFF00E676);
        ctx.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, 16, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7ПКМ по триггеру в списке = удалить"),
                width / 2, 29, 0xFF90A4AE);

        int cx = width / 2; int top = 65; int gap = 38;
        ctx.drawTextWithShadow(textRenderer, Text.literal("§eСлово или фраза-триггер:"), cx - 120, top - 12, 0xFFCCCCCC);
        ctx.drawTextWithShadow(textRenderer, Text.literal("§bВремя мута (30m, 1h, 1d...):"), cx - 120, top + gap - 12, 0xFFCCCCCC);
        ctx.drawTextWithShadow(textRenderer, Text.literal("§fПричина мута:"), cx - 120, top + gap * 2 - 12, 0xFFCCCCCC);

        ctx.fill(0, height - 2, width, height, 0xFF00E676);
        super.render(ctx, mx, my, delta);
    }

    @Override public boolean shouldPause() { return false; }
}
