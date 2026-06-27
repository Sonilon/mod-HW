package com.chatguard.event;

import com.chatguard.config.ChatGuardConfig;
import com.chatguard.gui.ViolationOverlay;
import com.chatguard.util.ChatParser;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;

import java.util.*;

public class ChatEventHandler {

    // Хранит последние нарушения: ник → (триггер, команда)
    // для обработки клика по сообщению
    public static final Map<String, PendingAction> pendingActions = new LinkedHashMap<>() {
        @Override protected boolean removeEldestEntry(Map.Entry<String, PendingAction> eldest) {
            return size() > 50;
        }
    };

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return; // игнорируем action bar
            handleChatMessage(message);
        });
    }

    private static void handleChatMessage(Text message) {
        String raw = message.getString();
        String msgText = ChatParser.extractMessage(raw);

        ChatGuardConfig.Trigger trigger = ChatParser.findTrigger(msgText);
        if (trigger == null) return;

        String nick = ChatParser.extractNick(raw);
        if (nick == null || nick.isEmpty()) return;

        // Нашли нарушителя!
        ChatGuardConfig cfg = ChatGuardConfig.getInstance();

        // Формируем команду мута
        String cmd = cfg.muteCommand
                .replace("{nick}",   nick)
                .replace("{time}",   trigger.time)
                .replace("{reason}", trigger.reason + " (п." + trigger.rule + ")");

        String actionKey = nick + "_" + System.currentTimeMillis();

        // Строим кликабельное сообщение-оповещение в чат
        String alertText = cfg.violationFormat
                .replace("{nick}",   nick)
                .replace("{word}",   trigger.word)
                .replace("{rule}",   trigger.rule)
                .replace("{time}",   trigger.time)
                .replace("{reason}", trigger.reason);

        // Создаём кликабельный текст
        Text clickable = buildClickableAlert(alertText, cmd, nick, trigger);

        // Отправляем в чат как системное сообщение клиенту
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.inGameHud.getChatHud().addMessage(clickable);

            // Звук
            if (cfg.soundEnabled) {
                mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        cfg.soundVolume, 0.8f);
            }
        }

        // Показать оверлей
        ViolationOverlay.addAlert(nick, trigger.word, trigger);

        // Сохранить для клика
        pendingActions.put(actionKey, new PendingAction(nick, cmd));
    }

    private static Text buildClickableAlert(String alertText, String cmd,
                                             String nick, ChatGuardConfig.Trigger trigger) {
        // Префикс-разделитель
        MutableText separator = Text.literal("§8§m                                        §r").copy();

        // Основное сообщение с кликом
        MutableText alert = Text.literal(alertText).copy();
        alert.setStyle(Style.EMPTY
                .withColor(net.minecraft.util.Formatting.RED)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.literal("§7Нажмите, чтобы открыть команду мута:\n§f" + cmd)))
                .withBold(false));

        // Подсказка под оповещением
        MutableText hint = Text.literal(
                "  §8[Нажмите чтобы открыть команду мута]").copy();
        hint.setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.literal("§f" + cmd))));

        return Text.empty()
                .append(separator).append("\n")
                .append(alert).append("\n")
                .append(hint).append("\n")
                .append(separator);
    }

    public static class PendingAction {
        public final String nick;
        public final String command;

        public PendingAction(String nick, String command) {
            this.nick    = nick;
            this.command = command;
        }
    }
}
