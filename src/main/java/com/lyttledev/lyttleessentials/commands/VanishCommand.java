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
import java.util.UUID;

public class VanishCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    public VanishCommand(LyttleEssentials plugin) {
        plugin.getCommand("vanish").setExecutor(this);
        this.plugin = plugin;
    }

    private List<UUID> vanishedPlayers = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.hasPermission("lyttleessentials.vanish"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        // Handle it when the console doesn't give a player
        if (args.length == 0 && !(sender instanceof Player)) {
            plugin.message.sendMessage(sender, "vanish_console");
            return true;
        }

        if (!(sender instanceof Player) && args.length == 1 && (args[0].equalsIgnoreCase("true") || args[0].equalsIgnoreCase("false"))) {
            plugin.message.sendMessage(sender, "vanish_console");
            return true;
        }

        // Too many arguments given
        if (args.length > 2) {
            plugin.message.sendMessage(sender, "vanish_usage");
            return true;
        }

        if  (args.length == 0) {
            toggleVanish((Player) sender);
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("true") ||  args[0].equalsIgnoreCase("false")) {
                Player target = (Player) sender;
                boolean bool = Boolean.parseBoolean(args[0]);
                setVanish(target, bool);
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                plugin.message.sendMessage(sender, "player_not_found");
            }
            toggleVanish(target);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        boolean bool = Boolean.parseBoolean(args[1]);
        setVanish(target, bool);
        return true;
    }

    private void toggleVanish(Player player) {
        if (vanishedPlayers.contains(player.getUniqueId())) {
            showPlayer(player);
            vanishedPlayers.remove(player.getUniqueId());
            return;
        }
        hidePlayer(player);
        vanishedPlayers.add(player.getUniqueId());
    }

    private void setVanish(Player player, boolean bool) {
        if (bool) {
            hidePlayer(player);
            vanishedPlayers.add(player.getUniqueId());
            return;
        }
        showPlayer(player);
        vanishedPlayers.remove(player.getUniqueId());
    }


    private void hidePlayer(Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.hidePlayer(plugin, player);
        }
    }

    private void showPlayer(Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(plugin, player);
        }
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
