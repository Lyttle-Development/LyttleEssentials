package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class LyttleEssentialsCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    public LyttleEssentialsCommand(LyttleEssentials plugin) {
        plugin.getCommand("lyttleessentials").setExecutor(this);
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // Check for permission
        if (!(sender.hasPermission("lyttleessentials.lyttleessentials"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("plugin version: 2.3.1");
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.config.reload();
                plugin.message.sendMessageRaw(sender, "The config has been reloaded");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }

        return List.of();
    }
}
