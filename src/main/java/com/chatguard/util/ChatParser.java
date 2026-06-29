package com.chatguard.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ChatParser {

    private static final Pattern PATTERN =
            Pattern.compile("<(.*?)> (.*)");

    public static Chat parse(String line) {

        Matcher m = PATTERN.matcher(line);

        if (!m.find()) return null;

        return new Chat(m.group(1), m.group(2));
    }

    public record Chat(String author, String message) {}
}
