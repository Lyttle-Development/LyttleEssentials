package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.Console;
import com.lyttledev.lyttleessentials.utils.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class AdminTeleportCommand implements CommandExecutor, TabCompleter {
    private LyttleEssentials plugin;

    public AdminTeleportCommand(LyttleEssentials plugin) {
        plugin.getCommand("atp").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Message.sendConsole("must_be_player", Message.noReplacements);
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("mapleadmin.staff")) {
            Message.sendPlayer((Player) sender, "no_permission", Message.noReplacements);
            return true;
        }

        if (args.length == 1) {
            Player target = plugin.getServer().getPlayer(args[0]);

            if (target == null) {
                Message.sendPlayer(player, "player_not_found", Message.noReplacements);
                return true;
            }

            player.teleport(target);

            String[][] messageReplacements = {
                { "<USER>", player.getName() },
                { "<TARGET>", target.getName() }
            };
            Message.sendPlayer(player, "atp_user", messageReplacements);
            return true;
        }

        if (args.length == 2) {
            Player user = plugin.getServer().getPlayer(args[0]);
            Player target = plugin.getServer().getPlayer(args[1]);

            if (user == null || target == null) {
                Message.sendPlayer(player, "player_not_found", Message.noReplacements);
                return true;
            }

            user.teleport(target);

            String[][] messageReplacements = {
                { "<USER>", user.getName() },
                { "<TARGET>", target.getName() }
            };
            Message.sendPlayer(player, "atp_user", messageReplacements);
            return true;
        }

        if (args.length == 3) {
            Console.playerCommand(player, "minecraft:tp " + args[0] + " " + args[1] + " " + args[2]);

            String[][] messageReplacements = {
                    { "<USER>", player.getName() },
                    { "<TARGET>", "Loc(" + args[0] + ", " + args[1] + ", " + args[2] + ")" }
            };
            return true;
        }

        if (args.length == 4) {
            Player user = plugin.getServer().getPlayer(args[0]);

            if (user == null) {
                Message.sendPlayer(player, "player_not_found", Message.noReplacements);
                return true;
            }

            Console.command("minecraft:execute as " +  user.getName() + " at @s run tp " + args[1] + " " + args[2] + " " + args[3]);

            String[][] messageReplacements = {
                    { "<USER>", user.getName() },
                    { "<TARGET>", "Loc(" + args[1] + ", " + args[2] + ", " + args[3] + ")" }
            };
            Message.sendPlayer(player, "atp_user", messageReplacements);
            return true;
        }

        Message.sendPlayer(player, "atp_usage", Message.noReplacements);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length <= 2) {
            return null;
        }

        return Arrays.asList();
    }
}
