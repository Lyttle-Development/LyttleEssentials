package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.SelectorUtil.SelectorUtil;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class PweatherCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    public PweatherCommand(LyttleEssentials plugin) {
        this.plugin = plugin;
        plugin.getCommand("pweather").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender.hasPermission("lyttleessentials.pweather"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if (!(sender instanceof Player) && args.length != 2) {
            plugin.message.sendMessage(sender, "pweather_console");
            return true;
        }

        if (args.length > 2 || args.length == 0) {
            plugin.message.sendMessage(sender, "pweather_usage");
            return true;
        }

        if (args.length == 1) {
            Player player = (Player) sender;
            String msg = setPweather(player, args[0]);
            messageHandler(player, player, msg);
            return true;
        }

        // args.length == 2 -> allow selectors or multiple targets
        List<Entity> targets = SelectorUtil.resolveSelector(sender, args[1], true);
        if (targets.isEmpty()) {
            plugin.message.sendMessage(sender, "player_not_found");
            return true;
        }

        for (Entity e : targets) {
            if (!(e instanceof Player)) continue;
            Player target = (Player) e;

            String msg = setPweather(target, args[0]);
            messageHandler(target, sender, msg);
        }
        return true;
    }

    private String setPweather(Player player, String weather) {
        return switch (weather) {
            case "rain" -> {
                player.setPlayerWeather(WeatherType.DOWNFALL);
                yield "rain";
            }
            case "clear" -> {
                player.setPlayerWeather(WeatherType.CLEAR);
                yield "clear";
            }
            case "reset" -> {
                player.resetPlayerWeather();
                yield "reset";
            }
            default -> "WRONG-USAGE";
        };
    }

    private void messageHandler(Player target, CommandSender sender, String pweather) {
        if (pweather.equals("WRONG-USAGE")) {
            plugin.message.sendMessage(sender, "pweather_usage");
            return;
        }

        Replacements replacementsTarget = new Replacements.Builder()
                .add("<PLAYER>", sender.getName())
                .add("<PWEATHER>", pweather)
                .build();

        Replacements replacementsSender = new Replacements.Builder()
                .add("<PLAYER>", target.getName())
                .add("<PWEATHER>", pweather)
                .build();

        Replacements replacements = new Replacements.Builder()
                .add("<PWEATHER>", pweather)
                .build();


        if (target == sender) {
            plugin.message.sendMessage(sender, "pweather_set_self",  replacements);
            return;
        }

        if (sender == Bukkit.getConsoleSender()) {
            plugin.message.sendMessage(target, "pweather_set_console", replacements);
            plugin.message.sendMessage(sender, "pweather_set_other_sender", replacementsSender);
            return;
        }

        plugin.message.sendMessage(sender, "pweather_set_other_sender", replacementsSender);
        plugin.message.sendMessage(target, "pweather_set_other_target", replacementsTarget);
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1) {
            return List.of("rain", "clear", "reset");
        }

        if (arguments.length == 2) {
            return SelectorUtil.selectorCompletions(arguments[1]);
        }

        return List.of();
    }

}