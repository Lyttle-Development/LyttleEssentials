package com.lyttledev.lyttleessentials.handlers;

import com.lyttledev.lyttleutils.types.Message.Replacements;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import com.lyttledev.lyttleessentials.LyttleEssentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.*;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class onPlayerChatListener implements Listener {
    private final LyttleEssentials plugin;

    public onPlayerChatListener(LyttleEssentials plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        // Cancel the default chat message
        event.setCancelled(true);

        // Get the player's message
        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        String roleDisplayname = MiniMessage.miniMessage().serialize(plugin.message.getMessage("chat_default_role"));

        // check if luckperms is installed
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            // Initialize LuckPerms API (Make sure you have the LuckPerms API library in your project)
            LuckPerms luckPerms = LuckPermsProvider.get();

            // Get the user object
            UserManager userManager = luckPerms.getUserManager();
            User user = userManager.getUser(player.getUniqueId());

            if (user != null) {
                // Get the user's primary group
                String primaryGroup = user.getPrimaryGroup();

                // Get the group's display name
                Group group = luckPerms.getGroupManager().getGroup(primaryGroup);
                if (group != null) {
                    @Nullable String displayName =  group.getDisplayName();
                    if (displayName != null) {
                        roleDisplayname = displayName;
                    }
                }
            }
        }

        String filteredMessage = filterMessage(message);
        if (filteredMessage.isEmpty()) {
            // If the filtered message is empty, do not send the chat message
            return;
        }

        Replacements replacements = new Replacements.Builder()
                .add("<PLAYER>", getDisplayName(player))
                .add("<ROLE>", roleDisplayname)
                .add("<MESSAGE>", filteredMessage)
                .build();

        plugin.message.sendBroadcast(false, "chat_format", replacements, player);
    }

    /**
     * Filters the chat message by:
     *  - Removing banned words
     *  - Removing MiniMessage formatting
     *  - Removing all newlines (literal or real)
     *  - Collapsing all whitespace (space, tab, etc.) into a single space
     *  - Trimming leading/trailing whitespace
     *  - Ensuring messages like "\n\n   \n test" become "test"
     */
    private String filterMessage(String message) {
        // Filter banned words
        message = filterBannedWords(message);

        // Remove any MiniMessage formatting (like &x or <color>)
        String[] miniMessageTags = {
                "<red>", "<green>", "<blue>", "<yellow>", "<gray>", "<dark_red>",
                "<dark_green>", "<dark_blue>", "<dark_yellow>", "<dark_gray>",
                "<black>", "<white>", "<reset>"
        };

        message = message
                .replaceAll("(?i)§[0-9a-fk-or]", "") // Remove color codes
                .replaceAll("(?i)&[0-9a-fk-or]", "") // Remove color codes
                .replaceAll(String.join("|", miniMessageTags), ""); // Remove MiniMessage tags

        message = MiniMessage.miniMessage().serialize(
                        MiniMessage.miniMessage().deserialize(message)
                )
                .replaceAll("(?i)§[0-9a-fk-or]", "") // Remove color codes
                .replaceAll("(?i)&[0-9a-fk-or]", "") // Remove color codes
                .replaceAll(String.join("|", miniMessageTags), ""); // Remove MiniMessage tags

        // Remove all newlines: literal "\n", real newlines, and Windows/Mac newlines
        message = message.replaceAll("(\\r\\n|\\r|\\n|\\\\n)", " ");

        // Collapse all whitespace (spaces, tabs, newlines, non-breaking spaces, etc.) into a single space
        message = message.replaceAll("[\\s\\u00A0]+", " ");

        // Remove leading/trailing whitespace
        return message.trim();
    }

    private String filterBannedWords(String message) {
        String pluginFolderPath = plugin.getDataFolder().getPath();
        String chatFilterFolderPath = pluginFolderPath + "/data/chat_filter";

        String[] files = Paths.get(chatFilterFolderPath).toFile().list((dir, name) -> name.endsWith(".txt"));

        if (files == null || files.length < 1) {
            return message; // No files to process, return the original message
        }

        // Normalize message: remove all non-letters, lowercase
        String normalizedMessage = message.replaceAll("[^a-zA-Z]", "").toLowerCase();
        boolean matchedCombined = false;

        for (String file : files) {
            try {
                String regexList = Files.readString(Paths.get(chatFilterFolderPath, file));
                regexList = regexList.replace("\r\n", "\n");

                String[] regexArray = regexList.split("\n");

                for (String regex : regexArray) {
                    regex = regex.trim();
                    if (!regex.isEmpty()) {
                        // 1. Regular replacement (normal chat)
                        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                        message = pattern.matcher(message).replaceAll("");

                        // 2. Combined/obfuscated version: also check for combined matches
                        String simpleRegex = regex.replaceAll("[^a-zA-Z]", "").toLowerCase();
                        if (!simpleRegex.isEmpty()) {
                            if (normalizedMessage.contains(simpleRegex)) {
                                matchedCombined = true;
                            }
                        }
                    }
                }
            } catch (IOException ignored) {}
        }

        if (matchedCombined) {
            return "";
        }

        return message;
    }
}