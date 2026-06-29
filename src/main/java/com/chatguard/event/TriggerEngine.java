package com.chatguard.event;

import com.chatguard.config.Trigger;
import com.chatguard.gui.ViolationOverlay;
import net.minecraft.client.MinecraftClient;

import java.util.List;

public class TriggerEngine {

    private List<Trigger> triggers;

    public void load(List<Trigger> list) {
        this.triggers = list;
    }

    public void process(String author, String msg) {

        String m = msg.toLowerCase();

        for (Trigger t : triggers) {

            for (String w : t.words) {

                if (m.contains(w.toLowerCase())) {

                    String cmd = t.command
                            .replace("{author}", author)
                            .replace("{msg}", msg);

                    MinecraftClient.getInstance().execute(() -> {
                        MinecraftClient.getInstance()
                                .player
                                .networkHandler
                                .sendChatCommand(cmd.replace("/", ""));
                    });

                    ViolationOverlay.push(author, msg, t.category);
                    return;
                }
            }
        }
    }
}
