package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleutils.types.Message.Replacements;
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
            plugin.message.sendMessage(sender,"must_be_player");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lyttleessentials.admintp")) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if (args.length == 1) {
            if (!player.hasPermission("lyttleessentials.admintp.self")) {
                plugin.message.sendMessage(sender, "no_permission");
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[0]);

            if (target == null) {
                plugin.message.sendMessage(player, "player_not_found");
                return true;
            }

            player.teleport(target);

            Replacements replacements = new Replacements.Builder()
                .add("<USER>", getDisplayName(player))
                .add("<TARGET>", getDisplayName(target))
                .build();

            plugin.message.sendMessage(player, "atp_user", replacements, player);
            return true;
        }

        if (!player.hasPermission("lyttleessentials.admintp.other")) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if (args.length == 2) {
            Player user = plugin.getServer().getPlayer(args[0]);
            Player target = plugin.getServer().getPlayer(args[1]);

            if (user == null || target == null) {
                plugin.message.sendMessage(player, "player_not_found");
                return true;
            }

            user.teleport(target);

            Replacements replacements = new Replacements.Builder()
                .add("<USER>", getDisplayName(user))
                .add("<TARGET>", getDisplayName(target))
                .build();

            plugin.message.sendMessage(player, "atp_user", replacements, player);
            return true;
        }

        if (args.length == 3) {
            plugin.console.run(player, "minecraft:tp " + args[0] + " " + args[1] + " " + args[2]);
            return true;
        }

        if (args.length == 4) {
            Player user = plugin.getServer().getPlayer(args[0]);

            if (user == null) {
                plugin.message.sendMessage(player, "player_not_found");
                return true;
            }

            plugin.console.run("minecraft:execute as " +  getDisplayName(user) + " at @s run tp " + args[1] + " " + args[2] + " " + args[3]);

            Replacements replacements = new Replacements.Builder()
                    .add("<USER>", getDisplayName(user))
                    .add("<TARGET>", "Loc(" + args[1] + ", " + args[2] + ", " + args[3] + ")")
                    .build();

            plugin.message.sendMessage(player, "atp_user", replacements, player);
            return true;
        }

        plugin.message.sendMessage(player, "atp_usage");

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
