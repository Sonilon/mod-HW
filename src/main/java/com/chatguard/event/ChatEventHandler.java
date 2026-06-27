package com.chatguard.event;

import com.chatguard.config.ChatGuardConfig;
import com.chatguard.gui.ViolationOverlay;
import com.chatguard.util.ChatParser;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;

import java.util.*;

public class ChatEventHandler {

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            handleChatMessage(message);
        });
    }

    private static void handleChatMessage(Text message) {
        String raw = message.getString();

        // Ищем триггер во всём сообщении (парсер сам найдёт часть после двоеточия)
        ChatGuardConfig.Trigger trigger = ChatParser.findTrigger(raw);
        if (trigger == null) return;

        String nick = ChatParser.extractNick(raw);
        if (nick == null || nick.isEmpty()) return;

        ChatGuardConfig cfg = ChatGuardConfig.getInstance();

        // Формируем команду мута
        String cmd = cfg.muteCommand
                .replace("{nick}",   nick)
                .replace("{time}",   trigger.time)
                .replace("{reason}", trigger.reason);

        // Формируем текст оповещения
        String alertText = cfg.violationFormat
                .replace("{nick}", nick)
                .replace("{word}", trigger.word)
                .replace("{time}", trigger.time)
                .replace("{reason}", trigger.reason);

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            // Кликабельное сообщение в чат
            Text clickable = buildClickableAlert(alertText, cmd);
            mc.inGameHud.getChatHud().addMessage(clickable);

            // Звук
            if (cfg.soundEnabled) {
                mc.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        cfg.soundVolume, 0.8f);
            }
        }

        // HUD оверлей
        ViolationOverlay.addAlert(nick, trigger.word, trigger);
    }

    private static Text buildClickableAlert(String alertText, String cmd) {
        MutableText sep = Text.literal("§8§m────────────────────────────────§r").copy();

        MutableText alert = Text.literal(alertText).copy();
        alert.setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.literal("§7Нажмите для команды мута:\n§f" + cmd))));

        MutableText hint = Text.literal("  §8▶ Нажмите чтобы замутить").copy();
        hint.setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.literal("§f" + cmd))));

        return Text.empty()
                .append(sep).append("\n")
                .append(alert).append("\n")
                .append(hint).append("\n")
                .append(sep);
    }
}
