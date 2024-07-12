package com.lyttledev.lyttleessentials.utils;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class Console {
    public static LyttleEssentials plugin;

    public static void init(LyttleEssentials plugin) {
        Console.plugin = plugin;
    }

    public static void command(String command) {
        try {
            if (command == null || command.isEmpty()) return;
            ConsoleCommandSender console = Bukkit.getConsoleSender();
            Bukkit.getScheduler().callSyncMethod( plugin, () -> Bukkit.dispatchCommand( console, command ) );
        } catch (Exception ignored) {}
    }

    public static void playerCommand(Player player, String command) {
        try {
            if (command == null || command.isEmpty()) return;
            Bukkit.getScheduler().callSyncMethod(plugin, () -> Bukkit.dispatchCommand(player, command));
        } catch (Exception ignored) {}
    }

    public static void log(String message) {
        plugin.getLogger().info(message);
    }
}
