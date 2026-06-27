package com.chatguard.util;

import com.chatguard.config.ChatGuardConfig;

import java.util.regex.*;

public class ChatParser {

    /**
     * Форматы чата сервера:
     *   [ALL] L | «стажер» NickName суффиксы: сообщение
     *   G | «стажер» NickName суффиксы: сообщение
     *   [ALL] L | NickName: сообщение
     *
     * Ник — первое слово из [A-Za-z0-9_] которое идёт:
     *   1) сразу после закрывающей » (самый надёжный способ)
     *   2) сразу после | если нет «»
     *   3) в самом начале до двоеточия
     */
    public static String extractNick(String rawMessage) {
        // Убираем ВСЕ цветовые коды (§x и §xx)
        String clean = rawMessage
                .replaceAll("§[0-9a-fk-orA-FK-OR]", "")
                .replaceAll("&[0-9a-fk-orA-FK-OR]", "")
                .trim();

        // Берём только часть ДО первого двоеточия
        int colonIdx = clean.indexOf(':');
        if (colonIdx < 0) return null;
        String before = clean.substring(0, colonIdx);

        // Стратегия 1: после «...» — самый точный паттерн для этого сервера
        // Пример: G | «стажер» NickName ...
        Matcher m1 = Pattern.compile("»\\s*([A-Za-z0-9_]{2,16})").matcher(before);
        if (m1.find()) return m1.group(1);

        // Стратегия 2: после | (пайпа)
        // Пример: [ALL] L | NickName ...
        Matcher m2 = Pattern.compile("\\|\\s*([A-Za-z0-9_]{2,16})").matcher(before);
        if (m2.find()) return m2.group(1);

        // Стратегия 3: после последней ] скобки
        Matcher m3 = Pattern.compile("\\]\\s*([A-Za-z0-9_]{2,16})").matcher(before);
        if (m3.find()) return m3.group(1);

        // Стратегия 4: первое валидное слово похожее на ник
        Matcher m4 = Pattern.compile("(?:^|\\s)([A-Za-z0-9_]{3,16})(?:\\s|$)").matcher(before);
        if (m4.find()) return m4.group(1);

        // Стратегия 5: кастомный regex из конфига
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
     * Ищет триггер ТОЛЬКО в тексте сообщения (после двоеточия).
     */
    public static ChatGuardConfig.Trigger findTrigger(String fullMessage) {
        if (fullMessage == null) return null;

        // Убираем цветовые коды
        String clean = fullMessage
                .replaceAll("§[0-9a-fk-orA-FK-OR]", "")
                .trim();

        // Берём текст ПОСЛЕ первого двоеточия
        int colonIdx = clean.indexOf(':');
        String msgPart = colonIdx >= 0 && colonIdx < clean.length() - 1
                ? clean.substring(colonIdx + 1).trim()
                : clean;

        String lower = msgPart.toLowerCase();
        for (ChatGuardConfig.Trigger t : ChatGuardConfig.getInstance().triggers) {
            if (!t.enabled || t.word == null || t.word.isEmpty()) continue;
            if (lower.contains(t.word.toLowerCase())) return t;
        }
        return null;
    }

    public static String extractMessage(String rawMessage) {
        String clean = rawMessage.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
        int colon = clean.indexOf(':');
        if (colon >= 0 && colon < clean.length() - 1) {
            return clean.substring(colon + 1).trim();
        }
        return clean;
    }
}
