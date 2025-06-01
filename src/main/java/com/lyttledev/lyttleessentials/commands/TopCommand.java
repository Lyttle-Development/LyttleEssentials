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

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class TopCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    public TopCommand(LyttleEssentials plugin) {
        this.plugin = plugin;
        plugin.getCommand("top").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.hasPermission("lyttleessentials.top"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if (args.length > 1) {
            plugin.message.sendMessage(sender,"top_usage");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                plugin.message.sendMessage(sender,"top_usage");
                return true;
            }
            if (!(sender.hasPermission("lyttleessentials.top.self"))) {
                plugin.message.sendMessage(sender, "no_permission");
                return true;
            }
            Player player = (Player) sender;
            _teleportToTop(player);
            plugin.message.sendMessage(player, "top_self");
            return true;
        }

        if (!(sender.hasPermission("lyttleessentials.top.other"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if (Bukkit.getPlayerExact(args[0]) == null) {
            plugin.message.sendMessage(sender,"player_not_found");
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        Replacements replacementsSender = new Replacements.Builder()
            .add("<PLAYER>", getDisplayName(player))
            .build();

        _teleportToTop(player);

        if (sender == Bukkit.getPlayerExact(args[0])) {
            plugin.message.sendMessage(player, "top_self");
            return true;
        }

        if (sender instanceof Player) {
            Replacements replacementsPlayer = new Replacements.Builder()
                .add("<PLAYER>", getDisplayName((Player) sender))
                .build();

            plugin.message.sendMessage(sender, "top_other_sender", replacementsSender);
            plugin.message.sendMessage(player, "top_other_player", replacementsPlayer);
            return true;
        }
        plugin.message.sendMessage(sender,"top_other_sender", replacementsSender);
        plugin.message.sendMessage(player, "top_console");
        return true;
    }

    private void _teleportToTop(Player player) {
        player.teleport(player.getWorld().getHighestBlockAt(player.getLocation()).getLocation().add(0, 1, 0));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return null;
        }
        return List.of();
    }
}
