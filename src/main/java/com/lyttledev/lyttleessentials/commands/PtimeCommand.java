package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class PtimeCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    public PtimeCommand(LyttleEssentials plugin) {
        this.plugin = plugin;
        plugin.getCommand("ptime").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.hasPermission("lyttleessentials.ptime"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if (!(sender instanceof Player) && args.length != 2) {
            plugin.message.sendMessage(sender, "ptime_console");
            return true;
        }

        if (args.length > 2 || args.length == 0) {
            plugin.message.sendMessage(sender, "ptime_usage");
            return true;
        }

        if (args.length == 1) {
            Player player = (Player) sender;
            String msg = setPtime(player, args[0]);
            messageHandler(player, player, msg);
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.message.sendMessage(sender, "player_not_found");
            return true;
        }

        String msg = setPtime(target, args[0]);
        messageHandler(target, sender, msg);
        return true;
    }

    private String setPtime(Player player, String time) {
        return switch (time) {
            case "day" -> {
                player.setPlayerTime(1000, false);
                yield "day";
            }
            case "noon" -> {
                player.setPlayerTime(6000, false);
                yield "noon";
            }
            case "night" -> {
                player.setPlayerTime(13000, false);
                yield "night";
            }
            case "midnight" -> {
                player.setPlayerTime(18000, false);
                yield "midnight";
            }
            case "reset" -> {
                player.resetPlayerTime();
                yield "reset";
            }
            default -> "WRONG-USAGE";
        };
    }

    private void messageHandler(Player target, CommandSender sender, String ptime) {
        if (ptime.equals("WRONG-USAGE")) {
            plugin.message.sendMessage(sender, "ptime_usage");
            return;
        }

        Replacements replacementsTarget = new Replacements.Builder()
                .add("<PLAYER>", sender.getName())
                .add("<PTIME>", ptime)
                .build();

        Replacements replacementsSender = new Replacements.Builder()
                .add("<PLAYER>", target.getName())
                .add("<PTIME>", ptime)
                .build();

        Replacements replacements = new Replacements.Builder()
                .add("<PTIME>", ptime)
                .build();


        if (target == sender) {
            plugin.message.sendMessage(sender, "ptime_set_self",  replacements);
            return;
        }

        if (sender == Bukkit.getConsoleSender()) {
            plugin.message.sendMessage(target, "ptime_set_console", replacements);
            plugin.message.sendMessage(sender, "ptime_set_other_sender", replacementsSender);
            return;
        }

        plugin.message.sendMessage(sender, "ptime_set_other_sender", replacementsSender);
        plugin.message.sendMessage(target, "ptime_set_other_target", replacementsTarget);
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1) {
            return List.of("day", "noon", "night", "midnight", "reset");
        }

        if (arguments.length == 2) {
            return null;
        }

        return List.of();
    }
}
