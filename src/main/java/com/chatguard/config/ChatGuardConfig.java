package com.chatguard.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ChatGuardConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("chatguard.json");

    private static ChatGuardConfig INSTANCE = new ChatGuardConfig();

    // --- Настройки звука ---
    public boolean soundEnabled = true;
    public float soundVolume = 1.0f;
    public String soundEvent = "entity.experience_orb.pickup"; // ванильный звук

    // --- Настройки сообщений ---
    public String alertPrefix = "§c§l⚠ НАРУШИТЕЛЬ: §r";
    public String violationFormat = "§c[ChatGuard] §f{nick} §7— §e{word} §7(Правило {rule})";
    public String muteCommand = "/tempmute {nick} {time} {reason}";

    // --- Триггеры ---
    public List<Trigger> triggers = new ArrayList<>();

    // --- Формат чата сервера ---
    // Пример: [L/G | Донатик] NickName [Титул]: сообщение
    // Регекс для извлечения ника
    public String nickRegex = "(?:\\[[^\\]]*\\]\\s*)?([A-Za-z0-9_]{3,16})(?:\\s*\\[[^\\]]*\\])?:";

    public static class Trigger {
        public String word = "";
        public String time = "1h";
        public String rule = "1.1";
        public String reason = "Нарушение правил чата";
        public boolean enabled = true;

        public Trigger() {}

        public Trigger(String word, String time, String rule, String reason) {
            this.word = word;
            this.time = time;
            this.rule = rule;
            this.reason = reason;
        }
    }

    public static ChatGuardConfig getInstance() {
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = new FileReader(CONFIG_PATH.toFile())) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                INSTANCE = gson.fromJson(r, ChatGuardConfig.class);
                if (INSTANCE == null) INSTANCE = new ChatGuardConfig();
                if (INSTANCE.triggers == null) INSTANCE.triggers = new ArrayList<>();
            } catch (Exception e) {
                System.err.println("[ChatGuard] Ошибка загрузки конфига: " + e.getMessage());
                INSTANCE = new ChatGuardConfig();
            }
        } else {
            INSTANCE = new ChatGuardConfig();
            // Добавить пример триггеров
            INSTANCE.triggers.add(new Trigger("мат1", "30m", "1.1", "Нецензурная брань"));
            INSTANCE.triggers.add(new Trigger("реклама", "1h", "2.3", "Реклама сторонних ресурсов"));
            save();
        }
    }

    public static void save() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(CONFIG_PATH, gson.toJson(INSTANCE));
        } catch (Exception e) {
            System.err.println("[ChatGuard] Ошибка сохранения конфига: " + e.getMessage());
        }
    }
}
