package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
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
        if (!(sender.hasPermission("TBE.gamemode"))) {
            //Chat.message(sender, "no_perms");
            return true;
        }

        if (args.length == 0 || args.length > 2) {
            //Chat.message(sender, "gamemode.messages.wrongargs");
            return true;
        }

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                //Chat.message(sender, "gamemode.messages.wrongargs");
                return true;
            }
            String mode = _gamemode((Player) sender, args[0]);

            if (mode.equalsIgnoreCase("wrongArgs")) {
                //Chat.message(sender, "gamemode.messages.wrongargs");
                return true;
            }

            String[][] replacements = {{"<MODE>", mode}};
            //Chat.message(sender, "gamemode.messages.self");
            return true;
        }

        if ((Bukkit.getPlayerExact(args[1]) == null)) {
            //Chat.message(sender, "playerNotFound");
            return true;
        }

        Player player = Bukkit.getPlayerExact(args[1]);

        String mode = _gamemode(player, args[0]);

        if (mode.equalsIgnoreCase("wrongArgs")) {
            //Chat.message(sender, "gamemode.messages.wrongargs");
            return true;
        }

        String[][] replacementsSender = {{"<MODE>", mode}, {"<PLAYER>", player.getDisplayName()}};
        //Chat.message(sender, "gamemode.messages.other.sender", replacementsSender);
        if (sender instanceof ConsoleCommandSender) {
            String[][] replacementsConsole = {{"<MODE>", mode}};
            //Chat.message(player, "gamemode.messages.console", replacementsConsole);
            return true;
        }
        String[][] replacementsPlayer = {{"<MODE>", mode}, {"<PLAYER>", ((Player) sender).getDisplayName()}};
        //Chat.message(player, "gamemode.messages.other.target", replacementsPlayer);
        return true;
    }

    private String _gamemode(Player player, String mode) {
        switch(mode) {
            case "survival": case "0": case "s":
                _changeGamemode(player, "SURVIVAL");
                return "survival";
            case "creative": case "1": case "c":
                _changeGamemode(player, "CREATIVE");
                return "creative";
            case "adventure": case "2": case "a":
                _changeGamemode(player, "ADVENTURE");
                return "adventure";
            case "spectator": case "3": case "sp":
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
