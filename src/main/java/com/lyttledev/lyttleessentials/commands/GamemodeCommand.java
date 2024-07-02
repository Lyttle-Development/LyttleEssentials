package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GamemodeCommand implements CommandExecutor, TabCompleter {

    public GamemodeCommand(LyttleEssentials plugin) {
        plugin.getCommand("gamemode").setExecutor(this);
        plugin.getCommand("gmc").setExecutor(this);
        plugin.getCommand("gma").setExecutor(this);
        plugin.getCommand("gms").setExecutor(this);
        plugin.getCommand("gmsp").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender.hasPermission("lyttleessentials.gamemode"))) {
            Message.sendPlayer((Player) sender, "no_permission");
            return true;
        }

        if (label.equalsIgnoreCase("gamemode")) {
            if (args.length == 0 || args.length > 2) {
                Message.sendPlayer((Player) sender, "gamemode_usage");
                return true;
            }

            if (args.length == 1) {

                if (!(sender.hasPermission("lyttleessentials.gamemode.self"))) {
                    Message.sendPlayer((Player) sender, "no_permission");
                    return true;
                }

                if (!(sender instanceof Player)) {
                    Message.sendPlayer((Player) sender, "gamemode_usage");
                    return true;
                }
                String mode = _gamemode((Player) sender, args[0]);

                if (mode.equalsIgnoreCase("ERROR-GAMEMODE-METHOD")) {
                    Message.sendPlayer((Player) sender, "gamemode_usage");
                    return true;
                }

                String[][] replacements = {{"<MODE>", mode}};
                Message.sendPlayer((Player) sender, "gamemode_self", replacements);
                return true;
            }

            if (!(sender.hasPermission("lyttleessentials.gamemode.other"))) {
                Message.sendPlayer((Player) sender, "no_permission");
                return true;
            }

            if ((Bukkit.getPlayerExact(args[1]) == null)) {
                if (sender instanceof Player) {
                    Message.sendPlayer((Player) sender, "player_not_found");
                    return true;
                }
                Message.sendConsole("player_not_found");
                return true;
            }

            Player player = Bukkit.getPlayerExact(args[1]);
            String mode = _gamemode(player, args[0]);

            if (mode.equals("ERROR-GAMEMODE-METHOD")) {
                if (sender instanceof Player) {
                    Message.sendPlayer((Player) sender, "gamemode_usage");
                    return true;
                }
                Message.sendConsole("gamemode_usage");
                return true;
            }

            if (sender instanceof Player) {
                String[][] replacementsSender = {{"<MODE>", mode}, {"<PLAYER>", player.getDisplayName()}};
                Message.sendPlayer((Player) sender, "gamemode_other_sender", replacementsSender);
                String[][] replacementsPlayer = {{"<MODE>", mode}, {"<PLAYER>", ((Player) sender).getDisplayName()}};
                Message.sendPlayer(player, "gamemode_other_target", replacementsPlayer);
                return true;
            }
            String[][] replacementsSender = {{"<MODE>", mode}, {"<PLAYER>", player.getDisplayName()}};
            Message.sendConsole("gamemode_other_sender", replacementsSender);
            String[][] replacementsConsole = {{"<MODE>", mode}};
            Message.sendPlayer(player, "gamemode_console", replacementsConsole);
            return true;
        }

        if (args.length > 1) {
            if (sender instanceof Player) {
                Message.sendPlayer((Player) sender, "gmx_usage");
                return true;
            }
            Message.sendConsole("gmx_usage");
            return true;
        }

        if (args.length == 0) {

            if (!(sender.hasPermission("lyttleessentials.gamemode.self"))) {
                Message.sendPlayer((Player) sender, "no_permission");
                return true;
            }

            if (!(sender instanceof Player)) {
                Message.sendConsole("gmx_usage");
                return true;
            }

            Player player = (Player) sender;
            String mode = _gamemode(player, label);

            if (mode.equals("ERROR-GAMEMODE-METHOD")) {
                Message.sendPlayer(player, "gmx_usage");
                return true;
            }

            String[][] replacements = {{"<MODE>", mode}};
            Message.sendPlayer((Player) sender, "gamemode_self", replacements);
            return true;
        }

        if (!(sender.hasPermission("lyttleessentials.gamemode.other"))) {
            Message.sendPlayer((Player) sender, "no_permission");
            return true;
        }

        if ((Bukkit.getPlayerExact(args[0]) == null)) {
            if (sender instanceof Player) {
                Message.sendPlayer((Player) sender, "player_not_found");
                return true;
            }
            Message.sendConsole("player_not_found");
            return true;
        }

        Player player = Bukkit.getPlayerExact(args[0]);
        String mode = _gamemode(player, label);

        if (sender instanceof Player) {

            if (mode.equals("ERROR-GAMEMODE-METHOD")) {
                Message.sendPlayer((Player) sender, "gmx_usage");
                return true;
            }

            String[][] replacementsSender = {{"<MODE>", mode}, {"<PLAYER>", player.getDisplayName()}};
            Message.sendPlayer((Player) sender, "gamemode_other_sender", replacementsSender);
            String[][] replacementsPlayer = {{"<MODE>", mode}, {"<PLAYER>", ((Player) sender).getDisplayName()}};
            Message.sendPlayer(player, "gamemode_other_target", replacementsPlayer);
            return true;
        }

        if (mode.equals("ERROR-GAMEMODE-METHOD")) {
            Message.sendPlayer((Player) sender, "gmx_usage");
            return true;
        }

        String[][] replacementsSender = {{"<MODE>", mode}, {"<PLAYER>", player.getDisplayName()}};
        Message.sendConsole("gamemode_other_sender", replacementsSender);
        String[][] replacementsConsole = {{"<MODE>", mode}};
        Message.sendPlayer(player, "gamemode_console", replacementsConsole);
        return true;
    }

    private String _gamemode(Player player, String mode) {
        switch(mode) {
            case "survival": case "0": case "s": case "gms":
                _changeGamemode(player, "SURVIVAL");
                return "survival";
            case "creative": case "1": case "c": case "gmc":
                _changeGamemode(player, "CREATIVE");
                return "creative";
            case "adventure": case "2": case "a": case "gma":
                _changeGamemode(player, "ADVENTURE");
                return "adventure";
            case "spectator": case "3": case "sp": case "gmsp":
                _changeGamemode(player, "SPECTATOR");
                return "spectator";
        }
        return "ERROR-GAMEMODE-METHOD";
    }

    private static void _changeGamemode(org.bukkit.entity.Player player, String mode) {
        player.setGameMode(GameMode.valueOf(mode));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (!label.equalsIgnoreCase("gamemode")) { return null; }

        if (args.length == 1) {
            List<String> options = Arrays.asList("survival", "creative", "spectator", "adventure");
            List<String> result = new ArrayList<>(Collections.emptyList());
            for (String option : options) {
                if (option.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(option);
                }
            }
            return result;
        }

        if (args.length == 2) {
            return null;
        }

        return List.of();
    }
}
