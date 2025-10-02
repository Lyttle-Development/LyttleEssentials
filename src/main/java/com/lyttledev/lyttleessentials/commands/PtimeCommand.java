package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import net.kyori.adventure.text.Component;
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
            plugin.message.sendMessageRaw(sender, Component.text("WRONG USAGE"));
            return true;
        }

        if (args.length > 2) {
            plugin.message.sendMessageRaw(sender, Component.text("WRONG USAGE"));
            return true;
        }

        if (args.length == 0) {
            Player player = (Player) sender;
            plugin.message.sendMessageRaw(player, Component.text("WRONG USAGE"));
            return true;
        }

        if (args.length == 1) {
            Player player = (Player) sender;
            String msg = setPtime(player, args[0]);
            plugin.message.sendMessageRaw(player, Component.text(msg));
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.message.sendMessageRaw(sender, Component.text("PLAYER NOT FOUND"));
            return true;
        }

        String msg = setPtime(target, args[0]);
        plugin.message.sendMessageRaw(sender, Component.text(msg));
        return true;
    }

    private String setPtime(Player player, String time) {
        return switch (time) {
            case "day" -> {
                player.setPlayerTime(1000, false);
                yield "SET TO DAY";
            }
            case "noon" -> {
                player.setPlayerTime(6000, false);
                yield "SET TO NOON";
            }
            case "night" -> {
                player.setPlayerTime(13000, false);
                yield "SET TO NIGHT";
            }
            case "midnight" -> {
                player.setPlayerTime(18000, false);
                yield "SET TO MIDNIGHT";
            }
            case "reset" -> {
                player.resetPlayerTime();
                yield "SET TO RESET";
            }
            default -> "WRONG USAGE";
        };
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
