package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.MemoryClass;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
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
            toggleVanish((Player) sender, sender);
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("true") ||  args[0].equalsIgnoreCase("false")) {
                Player target = (Player) sender;
                boolean bool = Boolean.parseBoolean(args[0]);
                setVanish(target, bool, sender);
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                plugin.message.sendMessage(sender, "player_not_found");
                return true;
            }
            toggleVanish(target, sender);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        boolean bool = Boolean.parseBoolean(args[1]);
        setVanish(target, bool, sender);
        return true;
    }

    private void toggleVanish(Player target, CommandSender sender) {
        if (MemoryClass.isVanished(target)) {
            hidePlayer(target, false);
            MemoryClass.showPlayer(target);
            messageHandler(target, sender, false);
            return;
        }
        hidePlayer(target, true);
        MemoryClass.hidePlayer(target);
        messageHandler(target, sender, true);
    }

    private void setVanish(Player target, boolean bool, CommandSender sender) {
        if (bool) {
            hidePlayer(target, true);
            if (MemoryClass.isVanished(target)) { return; }
            MemoryClass.hidePlayer(target);
            messageHandler(target, sender, true);
            return;
        }
        hidePlayer(target, false);
        if (!MemoryClass.isVanished(target)) { return; }
        MemoryClass.showPlayer(target);
        messageHandler(target, sender, false);
    }


    private void hidePlayer(Player player, Boolean bool) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (bool) {
                onlinePlayer.hidePlayer(plugin, player);
            } else {
                onlinePlayer.showPlayer(plugin, player);
            }
        }
    }

    private void messageHandler(Player target, CommandSender sender, Boolean bool) {
        Replacements replacementsTarget = new Replacements.Builder()
                .add("<PLAYER>", sender.getName())
                .build();

        Replacements replacementsSender = new Replacements.Builder()
                .add("<TARGET>", target.getName())
                .build();

        if (sender == Bukkit.getConsoleSender()) {
            if (bool) {
                plugin.message.sendMessage(target, "vanish_enable_console");
                plugin.message.sendMessage(sender, "vanish_enable_other_sender", replacementsSender);
                return;
            }
            plugin.message.sendMessage(target, "vanish_disable_console");
            plugin.message.sendMessage(sender, "vanish_disable_other_sender", replacementsSender);
            return;
        }

        if (target == sender) {
            if (bool) {
                plugin.message.sendMessage(target, "vanish_enable_self");
                return;
            }
            plugin.message.sendMessage(target, "vanish_disable_self");
            return;
        }

        if (bool) {
            plugin.message.sendMessage(target, "vanish_enable_other_target", replacementsTarget);
            plugin.message.sendMessage(sender, "vanish_enable_other_sender", replacementsSender);
            return;
        }
        plugin.message.sendMessage(target, "vanish_disable_other_target", replacementsTarget);
        plugin.message.sendMessage(sender, "vanish_disable_other_sender", replacementsSender);
    }

    private List<String> getOptionList() {
        List<String> optionList = new ArrayList<>();
        optionList.add("true");
        optionList.add("false");
        Bukkit.getOnlinePlayers().forEach(player -> optionList.add(player.getName()));
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
