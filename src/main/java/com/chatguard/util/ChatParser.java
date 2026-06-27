package com.chatguard.util;

import com.chatguard.config.ChatGuardConfig;

import java.util.regex.*;

public class ChatParser {

    /**
     * Извлекает ник из сообщения формата:
     * [L/G | Донат] NickName [Титул]: сообщение
     * или просто NickName: сообщение
     */
    public static String extractNick(String rawMessage) {
        // Убираем цветовые коды Minecraft (§x)
        String clean = rawMessage.replaceAll("§[0-9a-fk-or]", "").trim();

        // Паттерн 1: [префикс | что-то] НИК [титул]: текст
        // Паттерн 2: НИК [титул]: текст
        // Паттерн 3: НИК: текст
        String[] patterns = {
            // [xxx] ник [yyy]: ...  или  [xxx|yyy] ник: ...
            "^(?:\\[[^\\]]*\\]\\s*)+([A-Za-z0-9_]{2,16})(?:\\s*\\[[^\\]]*\\])*\\s*:",
            // ник [титул]: ...
            "^([A-Za-z0-9_]{2,16})(?:\\s*\\[[^\\]]*\\])+\\s*:",
            // просто ник:
            "^([A-Za-z0-9_]{2,16})\\s*:",
            // Кастомный регекс из конфига
            ChatGuardConfig.getInstance().nickRegex
        };

        for (String pat : patterns) {
            try {
                Matcher m = Pattern.compile(pat).matcher(clean);
                if (m.find()) {
                    String nick = m.group(1);
                    if (nick != null && !nick.isEmpty()) return nick;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    /**
     * Проверяет сообщение на наличие слов из триггеров.
     * Возвращает первый сработавший триггер или null.
     */
    public static ChatGuardConfig.Trigger findTrigger(String message) {
        if (message == null) return null;
        String lower = message.toLowerCase();
        for (ChatGuardConfig.Trigger t : ChatGuardConfig.getInstance().triggers) {
            if (!t.enabled || t.word == null || t.word.isEmpty()) continue;
            if (lower.contains(t.word.toLowerCase())) return t;
        }
        return null;
    }

    /**
     * Извлекает только текст сообщения (после двоеточия)
     */
    public static String extractMessage(String rawMessage) {
        String clean = rawMessage.replaceAll("§[0-9a-fk-or]", "").trim();
        int colon = clean.indexOf(':');
        if (colon >= 0 && colon < clean.length() - 1) {
            return clean.substring(colon + 1).trim();
        }
        return clean;
    }
}
