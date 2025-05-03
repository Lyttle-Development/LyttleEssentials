package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.Console;
import com.lyttledev.lyttleessentials.utils.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class AdminTeleportCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    public AdminTeleportCommand(LyttleEssentials plugin) {
        plugin.getCommand("atp").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Message.sendMessage(sender,"must_be_player");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lyttleessentials.admintp")) {
            Message.sendMessage(sender, "no_permission");
            return true;
        }

        if (args.length == 1) {
            if (!player.hasPermission("lyttleessentials.admintp.self")) {
                Message.sendMessage(sender, "no_permission");
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[0]);

            if (target == null) {
                Message.sendMessage(player, "player_not_found");
                return true;
            }

            player.teleport(target);

            String[][] messageReplacements = {
                { "<USER>", getDisplayName(player) },
                { "<TARGET>", getDisplayName(target) }
            };
            Message.sendMessage(player, "atp_user", messageReplacements);
            return true;
        }

        if (!player.hasPermission("lyttleessentials.admintp.other")) {
            Message.sendMessage(sender, "no_permission");
            return true;
        }

        if (args.length == 2) {
            Player user = plugin.getServer().getPlayer(args[0]);
            Player target = plugin.getServer().getPlayer(args[1]);

            if (user == null || target == null) {
                Message.sendMessage(player, "player_not_found");
                return true;
            }

            user.teleport(target);

            String[][] messageReplacements = {
                { "<USER>", getDisplayName(user) },
                { "<TARGET>", getDisplayName(target) }
            };
            Message.sendMessage(player, "atp_user", messageReplacements);
            return true;
        }

        if (args.length == 3) {
            Console.playerCommand(player, "minecraft:tp " + args[0] + " " + args[1] + " " + args[2]);

            String[][] messageReplacements = {
                    { "<USER>", getDisplayName(player) },
                    { "<TARGET>", "Loc(" + args[0] + ", " + args[1] + ", " + args[2] + ")" }
            };
            return true;
        }

        if (args.length == 4) {
            Player user = plugin.getServer().getPlayer(args[0]);

            if (user == null) {
                Message.sendMessage(player, "player_not_found");
                return true;
            }

            Console.command("minecraft:execute as " +  getDisplayName(user) + " at @s run tp " + args[1] + " " + args[2] + " " + args[3]);

            String[][] messageReplacements = {
                    { "<USER>", getDisplayName(user) },
                    { "<TARGET>", "Loc(" + args[1] + ", " + args[2] + ", " + args[3] + ")" }
            };
            Message.sendMessage(player, "atp_user", messageReplacements);
            return true;
        }

        Message.sendMessage(player, "atp_usage");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length <= 2) {
            return null;
        }

        return List.of();
    }
}
