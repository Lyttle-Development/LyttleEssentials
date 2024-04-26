package com.lyttledev.lyttleessentials.utils;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class Message {
    public static LyttleEssentials plugin;

    public static void init(LyttleEssentials plugin) {
        Message.plugin = plugin;
    }

    public static String getPrefix() {
        return getConfigMessage("prefix");
    }

    public static String getConfigMessage(String messageKey) {
        @Nullable String message = (String) plugin.config.messages.get(messageKey);
        if (message == null) {
            Console.log("Message key " + messageKey + " not found in messages.yml");
            message = (String) plugin.config.messages.get("message_not_found");
        }

        if (message == null) {
            Console.log("Even the message_not_found not found in messages.yml...");
            message = "&cOh... I can't react to that. (Contact the Administrators)";
        }

        return message;
    }

    public static String replaceMessageStrings(String message, String[][] replacements) {
        for (String[] replacement : replacements) {
            message = message.replace(replacement[0], replacement[1]);
        }
        return message;
    }

    public static String[][] noReplacements = {};

    public static void sendPlayer(Player player, String message, @Nullable String[][] replacements) {
        String msg = replaceMessageStrings(getConfigMessage(message), replacements);
        player.sendMessage(getMessage(getPrefix() + msg));
    }

    public static void sendPlayerRaw(Player player, String message) {
        player.sendMessage(getMessage(getPrefix() + message));
    }

    public static void sendConsole(String message, String[][] replacements) {
        Console.log(getMessage(replaceMessageStrings(getConfigMessage(message), replacements)));
    }
    
    public static void sendConsoleRaw(String message) {
        Console.log(getMessage(message));
    }

    public static void sendChat(String message, String[][] replacements) {
        String msg = replaceMessageStrings(getConfigMessage(message), replacements);
        Bukkit.broadcastMessage(getMessage(getPrefix() + msg));
    }
    public static void sendChat(String message, String[][] replacements, boolean prefix) {
        String msg = replaceMessageStrings(getConfigMessage(message), replacements);
        if (prefix) {
            Bukkit.broadcastMessage(getMessage(getPrefix() + msg));
            return;
        }
        Bukkit.broadcastMessage(getMessage(msg));
    }

    public static void sendChatRaw(String message, boolean prefix) {
        if (prefix) {
            Bukkit.broadcastMessage(getMessage(getPrefix() + message));
            return;
        }
        Bukkit.broadcastMessage(getMessage(message));
    }

    public static String getMessage(String message) {
        // Replace all \n with real newlines
        message = message.replace("\\n", "\n");
        return ChatColor.translateAlternateColorCodes('&', message + "&r");
    }
}
