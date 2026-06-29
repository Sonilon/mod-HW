package com.chatguard.event;

import com.chatguard.util.ChatParser;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatBuffer {
    public static ConcurrentLinkedQueue<ChatParser.Chat> queue =
            new ConcurrentLinkedQueue<>();
}
