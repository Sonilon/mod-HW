package com.chatguard.gui;

import com.chatguard.config.ChatGuardConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;

public class TriggerEditScreen extends Screen {

    private final Screen parent;
    private final ChatGuardConfig.Trigger trigger;
    private final int index; // -1 = новый

    private TextFieldWidget wordField;
    private TextFieldWidget timeField;
    private TextFieldWidget ruleField;
    private TextFieldWidget reasonField;

    private static final int ACCENT = 0xFF00E676;
    private static final int BG     = 0xE0101820;

    public TriggerEditScreen(Screen parent, ChatGuardConfig.Trigger trigger, int index) {
        super(Text.literal(trigger == null ? "§a+ Добавить триггер" : "§e✎ Редактировать триггер"));
        this.parent  = parent;
        this.trigger = trigger != null ? trigger : new ChatGuardConfig.Trigger();
        this.index   = index;
    }

    @Override
    protected void init() {
        int cx  = width / 2;
        int top = 60;
        int fw  = 240;
        int fh  = 20;
        int gap = 36;

        wordField = new TextFieldWidget(textRenderer, cx - fw / 2, top, fw, fh,
                Text.literal("Слово-триггер"));
        wordField.setText(trigger.word);
        wordField.setMaxLength(64);
        addDrawableChild(wordField);

        timeField = new TextFieldWidget(textRenderer, cx - fw / 2, top + gap, fw, fh,
                Text.literal("Время мута"));
        timeField.setText(trigger.time);
        timeField.setMaxLength(20);
        addDrawableChild(timeField);

        ruleField = new TextFieldWidget(textRenderer, cx - fw / 2, top + gap * 2, fw, fh,
                Text.literal("Пункт правил"));
        ruleField.setText(trigger.rule);
        ruleField.setMaxLength(20);
        addDrawableChild(ruleField);

        reasonField = new TextFieldWidget(textRenderer, cx - fw / 2, top + gap * 3, fw, fh,
                Text.literal("Причина мута"));
        reasonField.setText(trigger.reason);
        reasonField.setMaxLength(128);
        addDrawableChild(reasonField);

        // Сохранить
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§a✔ Сохранить"),
                btn -> save()
        ).dimensions(cx - 105, top + gap * 4 + 10, 100, 20).build());

        // Отмена
        addDrawableChild(ButtonWidget.builder(
                Text.literal("§c✖ Отмена"),
                btn -> client.setScreen(parent)
        ).dimensions(cx + 5, top + gap * 4 + 10, 100, 20).build());
    }

    private void save() {
        trigger.word   = wordField.getText().trim();
        trigger.time   = timeField.getText().trim();
        trigger.rule   = ruleField.getText().trim();
        trigger.reason = reasonField.getText().trim();

        if (trigger.word.isEmpty()) return; // не сохранять пустое

        if (index == -1) {
            ChatGuardConfig.getInstance().triggers.add(trigger);
        }
        // else — уже редактируем ссылку, список обновлён
        ChatGuardConfig.save();
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, BG);
        ctx.fill(0, 0, width, 2, ACCENT);

        ctx.fill(0, 5, width, 45, 0xFF1A2A3A);
        ctx.fill(0, 5, 4, 45, ACCENT);
        ctx.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, 16, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7ПКМ по триггеру в списке = удалить"),
                width / 2, 29, 0xFF90A4AE);

        int cx  = width / 2;
        int top = 60;
        int gap = 36;

        drawLabel(ctx, "§eСлово или фраза-триггер:", cx, top - 10);
        drawLabel(ctx, "§bВремя мута (напр. 30m, 1h, 1d):", cx, top + gap - 10);
        drawLabel(ctx, "§aПункт правил:", cx, top + gap * 2 - 10);
        drawLabel(ctx, "§fПричина мута:", cx, top + gap * 3 - 10);

        ctx.fill(0, height - 2, width, height, ACCENT);
        super.render(ctx, mx, my, delta);
    }

    private void drawLabel(DrawContext ctx, String text, int cx, int y) {
        ctx.drawTextWithShadow(textRenderer, Text.literal(text), cx - 120, y, 0xFFCCCCCC);
    }

    @Override
    public boolean shouldPause() { return false; }
}
