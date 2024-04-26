package com.lyttledev.lyttleessentials.utils;

public class MessageCleaner {
    public static String cleanMessage(String message) {
        // Use regular expression to keep only alphanumeric characters
        return message.replaceAll("[^A-Za-z0-9]", "");
    }
}
