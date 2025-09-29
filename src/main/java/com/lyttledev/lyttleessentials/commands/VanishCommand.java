package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VanishCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    public VanishCommand(LyttleEssentials plugin) {
        plugin.getCommand("vanish").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.hasPermission("lyttleessentials.vanish"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        // Handle it when the console doesn't give a player
        if (args.length == 0 && !(sender instanceof Player)) {
            // Message that the console can't vanish itself
            return true;
        }

        // Too many arguments given
        if (args.length > 2) {
            // TODO Message the sender
            return true;
        }

        return true;
    }

    // Toggle own Vanish
    private void vanishPlayer() {

    }

    // Set own Vanish
    private void vanishPlayer(Boolean bool) {

    }

    // Toggle another players vanish
    private void vanishPlayer(Player player) {

    }

    // Set another players vanish
    private void vanishPlayer(Player player, Boolean bool) {

    }

    private List<String> getOptionList() {
        List<String> optionList = new ArrayList<>();
        optionList.add("true");
        optionList.add("false");
        Bukkit.getOnlinePlayers().forEach(player -> {
            optionList.add(player.getName());
        });
        return optionList;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1) {
            return getOptionList();
        }

        if (arguments.length == 2) {
            if (arguments[0].equalsIgnoreCase("true") || arguments[0].equalsIgnoreCase("false")) {
                return List.of();
            }
            return List.of("true", "false");
        }

        return List.of();
    }
}
