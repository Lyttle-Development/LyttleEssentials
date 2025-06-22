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
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());;

        String roleDisplayname = MiniMessage.miniMessage().serialize(plugin.message.getConfigMessage("chat_default_role"));

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

        Replacements replacements = new Replacements.Builder()
            .add("<PLAYER>", getDisplayName(player))
            .add("<ROLE>", roleDisplayname)
            .add("<MESSAGE>", filterMessage(message))
            .build();

        plugin.message.sendBroadcast("chat_format", replacements, false, player);
    }

    private String filterMessage(String message) {
        try {
            String pluginFolderPath = plugin.getDataFolder().getPath();
            // Your list of regex expressions with new lines per expression
            String regexList = Files.readString(Paths.get(pluginFolderPath, "/data/chat_filter.txt"));
            // Bring it back to LF format if it's CRLF
            regexList = regexList.replace("\r\n", "\n");

            // Split the regex list into an array of regex expressions
            String[] regexArray = regexList.split("\n");

            // Iterate over each regex in the array but make it in-case-sensitive
            for (String regex : regexArray) {
                // Trim the regex to remove any leading or trailing whitespace
                regex = regex.trim();
                if (!regex.isEmpty()) {
                    // Compile the regex with case-insensitive flag
                    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                    // Replace matches with an empty string
                    message = pattern.matcher(message).replaceAll("");
                }
            }

            // Remove newlines (with space)
            message = message.replaceAll("\\n", " ");
            // Replace multiple spaces with a single space
            message = message.replaceAll("\\s+", " ");
            // Remove any kind of link (http:// https:// mailto: tel: ...)
            message = message.replaceAll("(?i)\\b(?:https?://|www\\.|mailto:|tel:|ftp://|file://|irc://|xmpp:)[^\\s]+", "");
            // Remove any kind of email address
            message = message.replaceAll("(?i)\\b[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}\\b", "");
            // Remove any kind of phone number
            message = message.replaceAll("(?i)\\b\\+?[0-9][0-9\\s.-]{7,}[0-9]\\b", "");
            // Remove color codes
            message = message.replaceAll("(?i)§[0-9a-fk-or]", "");
            // Remove any kind of special characters
            message = message.replaceAll("[^\\p{L}\\p{N}\\s]", "");
            // Remove any kind of domains
            message = message.replaceAll("(?i)\\b(?:[a-z0-9-]+\\.)+[a-z]{2,}\\b", "");

            return message;
        } catch (IOException ignored) {
            return message;
        }
    }
}
