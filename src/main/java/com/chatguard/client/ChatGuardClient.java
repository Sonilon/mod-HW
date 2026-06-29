package com.chatguard.client;

import com.chatguard.util.LogTailer;
import com.chatguard.util.ChatParser;
import com.chatguard.event.ChatBuffer;
import com.chatguard.event.TriggerEngine;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.io.File;

public class ChatGuardClient implements ClientModInitializer {

    public static TriggerEngine engine;
    public static LogTailer logTailer;

    @Override
    public void onInitializeClient() {

        engine = new TriggerEngine();
        engine.load(DefaultConfig.load()); // из config

        File log = new File(MinecraftClient.getInstance().runDirectory, "logs/latest.log");
        logTailer = new LogTailer(log);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.world == null) return;

            // 1. LOG (AHK SYSTEM)
            for (String line : logTailer.poll()) {
                var chat = ChatParser.parse(line);
                if (chat != null) {
                    engine.process(chat.author, chat.message);
                }
            }

            // 2. EVENT BUFFER (LabyMod fix)
            while (!ChatBuffer.queue.isEmpty()) {
                var chat = ChatBuffer.queue.poll();
                engine.process(chat.author, chat.message);
            }
        });
    }
}
