package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class GamemodeCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    public GamemodeCommand(LyttleEssentials plugin) {
        this.plugin = plugin;
        plugin.getCommand("gamemode").setExecutor(this);
        plugin.getCommand("gmc").setExecutor(this);
        plugin.getCommand("gma").setExecutor(this);
        plugin.getCommand("gms").setExecutor(this);
        plugin.getCommand("gmsp").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.hasPermission("lyttleessentials.gamemode"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if (label.equalsIgnoreCase("gamemode")) {
            if (args.length == 0 || args.length > 2) {
                plugin.message.sendMessage(sender, "gamemode_usage");
                return true;
            }

            if (args.length == 1) {

                if (!(sender.hasPermission("lyttleessentials.gamemode.self"))) {
                    plugin.message.sendMessage(sender, "no_permission");
                    return true;
                }

                if (!(sender instanceof Player)) {
                    plugin.message.sendMessage(sender, "gamemode_usage");
                    return true;
                }
                String mode = _gamemode((Player) sender, args[0]);

                if (mode.equalsIgnoreCase("ERROR-GAMEMODE-METHOD")) {
                    plugin.message.sendMessage(sender, "gamemode_usage");
                    return true;
                }

                Replacements replacements= new Replacements.Builder()
                        .add("<MODE>", mode)
                        .build();

                plugin.message.sendMessage(sender, "gamemode_self", replacements);
                return true;
            }

            if (!(sender.hasPermission("lyttleessentials.gamemode.other"))) {
                plugin.message.sendMessage(sender, "no_permission");
                return true;
            }

            if ((Bukkit.getPlayerExact(args[1]) == null)) {
                if (sender instanceof Player) {
                    plugin.message.sendMessage(sender, "player_not_found");
                    return true;
                }
                plugin.message.sendMessage(sender,"player_not_found");
                return true;
            }

            Player player = Bukkit.getPlayerExact(args[1]);
            String mode = _gamemode(player, args[0]);

            if (mode.equals("ERROR-GAMEMODE-METHOD")) {
                if (sender instanceof Player) {
                    plugin.message.sendMessage(sender, "gamemode_usage");
                    return true;
                }
                plugin.message.sendMessage(sender,"gamemode_usage");
                return true;
            }

            Replacements replacementsSender = new Replacements.Builder()
                    .add("<MODE>", mode)
                    .add("<PLAYER>", getDisplayName(player))
                    .build();

            if (sender instanceof Player) {
                plugin.message.sendMessage(sender, "gamemode_other_sender", replacementsSender);

                Replacements replacementsPlayer = new Replacements.Builder()
                        .add("<MODE>", mode)
                        .add("<PLAYER>", getDisplayName((Player) sender))
                        .build();

                plugin.message.sendMessage(player, "gamemode_other_target", replacementsPlayer);
                return true;
            }

            plugin.message.sendMessage(sender,"gamemode_other_sender", replacementsSender);

            Replacements replacementsConsole = new Replacements.Builder()
                    .add("<MODE>", mode)
                    .build();

            plugin.message.sendMessage(player, "gamemode_console", replacementsConsole);
            return true;
        }

        if (args.length > 1) {
            plugin.message.sendMessage(sender,"gmx_usage");
            return true;
        }

        if (args.length == 0) {

            if (!(sender.hasPermission("lyttleessentials.gamemode.self"))) {
                plugin.message.sendMessage(sender, "no_permission");
                return true;
            }

            if (!(sender instanceof Player)) {
                plugin.message.sendMessage(sender,"gmx_usage");
                return true;
            }

            Player player = (Player) sender;
            String mode = _gamemode(player, label);

            if (mode.equals("ERROR-GAMEMODE-METHOD")) {
                plugin.message.sendMessage(player, "gmx_usage");
                return true;
            }

            Replacements replacements = new Replacements.Builder()
                    .add("<MODE>", mode)
                    .build();
            plugin.message.sendMessage(sender, "gamemode_self", replacements);
            return true;
        }

        if (!(sender.hasPermission("lyttleessentials.gamemode.other"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if ((Bukkit.getPlayerExact(args[0]) == null)) {
            plugin.message.sendMessage(sender,"player_not_found");
            return true;
        }

        Player player = Bukkit.getPlayerExact(args[0]);
        String mode = _gamemode(player, label);

        if (mode.equals("ERROR-GAMEMODE-METHOD")) {
            plugin.message.sendMessage(sender, "gmx_usage");
            return true;
        }

        Replacements replacementsSender = new Replacements.Builder()
                .add("<MODE>", mode)
                .add("<PLAYER>", getDisplayName(player))
                .build();

        if (sender instanceof Player) {
            plugin.message.sendMessage(sender, "gamemode_other_sender", replacementsSender);

            Replacements replacementsPlayer = new Replacements.Builder()
                    .add("<MODE>", mode)
                    .add("<PLAYER>", getDisplayName((Player) sender))
                    .build();

            plugin.message.sendMessage(player, "gamemode_other_target", replacementsPlayer);
            return true;
        }

        plugin.message.sendMessage(sender,"gamemode_other_sender", replacementsSender);

        Replacements replacementsConsole = new Replacements.Builder()
                .add("<MODE>", mode)
                .build();

        plugin.message.sendMessage(player, "gamemode_console", replacementsConsole);
        return true;
    }

    private String _gamemode(Player player, String mode) {
        return switch (mode) {
            case "survival", "0", "s", "gms" -> {
                _changeGamemode(player, "SURVIVAL");
                yield "survival";
            }
            case "creative", "1", "c", "gmc" -> {
                _changeGamemode(player, "CREATIVE");
                yield "creative";
            }
            case "adventure", "2", "a", "gma" -> {
                _changeGamemode(player, "ADVENTURE");
                yield "adventure";
            }
            case "spectator", "3", "sp", "gmsp" -> {
                _changeGamemode(player, "SPECTATOR");
                yield "spectator";
            }
            default -> "ERROR-GAMEMODE-METHOD";
        };
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
