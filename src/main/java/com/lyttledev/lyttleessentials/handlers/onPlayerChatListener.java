package com.lyttledev.lyttleessentials.handlers;

import io.papermc.paper.event.player.AsyncChatEvent;
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
        String message = event.message().toString();
        String roleDisplayname = plugin.message.getConfigMessage("chat_default_role");

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

        String[][] replacements = {
            {"<PLAYER>", getDisplayName(player)},
            {"<ROLE>", roleDisplayname},
            {"<MESSAGE>", filterMessage(message)}
        };

        plugin.message.sendBroadcast("chat_format", replacements, false);
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

            // Iterate over each regex in the array
            for (String regex : regexArray) {
                // Create a Pattern object for the current regex
                Pattern pattern = Pattern.compile(regex);

                // Create a Matcher object for the input string
                Matcher matcher = pattern.matcher(message);

                // Replace each character of matches with "#"
                StringBuffer result = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(result, new String(new char[matcher.group().length()]).replace('\0', '#'));
                }
                matcher.appendTail(result);

                // Update the input string with the modified result
                message = result.toString();
            }

            return message;
        } catch (IOException ignored) {
            return message;
        }
    }

    private String getRegexList() {
        // Specify the path to your text file
        String filePath = "" ;

        String regexList = "";
        // Read the file as a string
        try {
            regexList = readFileToString(filePath);
        } catch (IOException ignored) {}

        return regexList;
    }

    public static String readFileToString(String filePath) throws IOException {
        // Use Paths.get() to create a Path object from the file path
        Path path = Paths.get(filePath);

        // Use Files.readString() to read the entire contents of the file into a string

        return Files.readString(path);
    }
}
