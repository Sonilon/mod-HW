package com.chatguard.event;

import com.chatguard.config.ChatGuardConfig;
import com.chatguard.gui.ViolationOverlay;
import com.chatguard.util.ChatParser;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;

public class ChatEventHandler {

    public static void register() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            handleChatMessage(message);
        });
    }

    private static void handleChatMessage(Text message) {
        String raw = message.getString();

        ChatGuardConfig.Trigger trigger = ChatParser.findTrigger(raw);
        if (trigger == null) return;

        String nick = ChatParser.extractNick(raw);
        if (nick == null || nick.isEmpty()) return;

        String msgText = ChatParser.extractMessage(raw);

        ChatGuardConfig cfg = ChatGuardConfig.getInstance();

        // Команда мута
        String cmd = cfg.muteCommand
                .replace("{nick}",   nick)
                .replace("{time}",   trigger.time)
                .replace("{reason}", trigger.reason);

        // Время мута в часах для отображения
        String mutTime = trigger.time;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            // Строим красивое уведомление как на скриншоте
            Text alert = buildAlert(nick, trigger, msgText, cmd, mutTime);
            mc.inGameHud.getChatHud().addMessage(alert);

            if (cfg.soundEnabled) {
                mc.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                        cfg.soundVolume, 1.2f);
            }
        }

        ViolationOverlay.addAlert(nick, trigger.word, trigger);
    }

    private static Text buildAlert(String nick, ChatGuardConfig.Trigger trigger,
                                    String msgText, String cmd, String mutTime) {
        // Точный формат как на скриншоте:
        // ─────────────────────────────────
        // [МОДЕРАЦИЯ] ⚠ Нарушение обнаружено!
        // Игрок: NickName | Причина: ... | Рек. мут: Xч
        // Сообщение: "слово"
        // Детали: Слово: "слово"
        // Команда: /mute Nick 180m Причина
        // ─────────────────────────────────

        String sep = "§8§m─────────────────────────────────§r";
        String word = trigger.word;

        MutableText line1 = Text.literal(sep);

        MutableText line2 = Text.literal("§c[МОДЕРАЦИЯ] §e⚠ §fНарушение обнаружено!");

        MutableText line3 = Text.literal(
                "§7Игрок: §f" + nick +
                " §8| §7Причина: §f" + trigger.reason +
                " §8| §7Рек. мут: §f" + mutTime);

        MutableText line4 = Text.literal("§7Сообщение: §f\"" + msgText + "\"");

        MutableText line5 = Text.literal("§7Детали: Слово: §f\"" + word + "\"");

        // Строка команды — кликабельная
        MutableText cmdText = Text.literal("§7Команда: §a" + cmd);
        cmdText.setStyle(Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.literal("§aНажмите чтобы вставить команду в чат"))));

        MutableText line6 = cmdText;
        MutableText line7 = Text.literal(sep);

        return Text.empty()
                .append(line1).append("\n")
                .append(line2).append("\n")
                .append(line3).append("\n")
                .append(line4).append("\n")
                .append(line5).append("\n")
                .append(line6).append("\n")
                .append(line7);
    }
}
