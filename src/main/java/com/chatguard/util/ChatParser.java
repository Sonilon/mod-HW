package com.chatguard.util;

import com.chatguard.config.ChatGuardConfig;

import java.util.regex.*;

public class ChatParser {

    /**
     * Формат чата сервера:
     * ? | «титул» NickName суффиксы: сообщение
     * Примеры:
     *   ? | «стажер» 1pirs Обменяю Ко мне!: test
     *   ? | 1pirs: test
     *   [VIP] 1pirs: test
     *
     * Ник — первое слово из [A-Za-z0-9_] длиной 2-16 символов,
     * которое идёт после необязательных префиксов/титулов до двоеточия.
     */
    public static String extractNick(String rawMessage) {
        // Убираем цветовые коды Minecraft (§x или &x)
        String clean = rawMessage
                .replaceAll("§[0-9a-fk-orA-FK-OR]", "")
                .replaceAll("&[0-9a-fk-orA-FK-OR]", "")
                .trim();

        // Берём только часть до первого двоеточия
        int colonIdx = clean.indexOf(':');
        if (colonIdx < 0) return null;
        String beforeColon = clean.substring(0, colonIdx);

        // Убираем всё в угловых и квадратных скобках и «ёлочках»
        // типа [L/G], «стажер», (что-то)
        String stripped = beforeColon
                .replaceAll("\\[[^\\]]*\\]", " ")   // [...]
                .replaceAll("«[^»]*»", " ")          // «...»
                .replaceAll("\\([^)]*\\)", " ")      // (...)
                .replaceAll("[|?★✦✧♦◆●•]", " ")    // спецсимволы-разделители
                .trim();

        // Ищем первое слово которое выглядит как ник Minecraft
        // Ник: только буквы, цифры, подчёркивание, 2-16 символов
        Matcher m = Pattern.compile("(?<![\\w])([A-Za-z0-9_]{2,16})(?![\\w])").matcher(stripped);
        while (m.find()) {
            String candidate = m.group(1);
            // Пропускаем слова которые явно не ники (короткие служебные)
            if (candidate.length() < 2) continue;
            return candidate;
        }

        // Фолбэк: кастомный regex из конфига
        try {
            String customRegex = ChatGuardConfig.getInstance().nickRegex;
            if (customRegex != null && !customRegex.isEmpty()) {
                Matcher cm = Pattern.compile(customRegex).matcher(clean);
                if (cm.find()) return cm.group(1);
            }
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Проверяет сообщение на триггеры. Проверяем ВЕСЬ текст сообщения
     * (после двоеточия), без учёта регистра.
     */
    public static ChatGuardConfig.Trigger findTrigger(String fullMessage) {
        if (fullMessage == null) return null;

        // Берём текст после последнего двоеточия
        String msgPart = fullMessage;
        int colonIdx = fullMessage.lastIndexOf(':');
        if (colonIdx >= 0 && colonIdx < fullMessage.length() - 1) {
            msgPart = fullMessage.substring(colonIdx + 1);
        }

        String lower = msgPart.toLowerCase();
        for (ChatGuardConfig.Trigger t : ChatGuardConfig.getInstance().triggers) {
            if (!t.enabled || t.word == null || t.word.isEmpty()) continue;
            if (lower.contains(t.word.toLowerCase())) return t;
        }
        return null;
    }

    /**
     * Извлекает текст сообщения (после первого двоеточия)
     */
    public static String extractMessage(String rawMessage) {
        String clean = rawMessage
                .replaceAll("§[0-9a-fk-orA-FK-OR]", "")
                .trim();
        int colon = clean.indexOf(':');
        if (colon >= 0 && colon < clean.length() - 1) {
            return clean.substring(colon + 1).trim();
        }
        return clean;
    }
}
