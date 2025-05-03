package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class TopCommand implements CommandExecutor, TabCompleter {

    public TopCommand(LyttleEssentials plugin) {
        plugin.getCommand("top").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.hasPermission("lyttleessentials.top"))) {
            Message.sendMessage(sender, "no_permission");
            return true;
        }

        if (args.length > 1) {
            Message.sendMessage(sender,"top_usage");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                Message.sendMessage(sender,"top_usage");
                return true;
            }
            if (!(sender.hasPermission("lyttleessentials.top.self"))) {
                Message.sendMessage(sender, "no_permission");
                return true;
            }
            Player player = (Player) sender;
            _teleportToTop(player);
            Message.sendMessage(player, "top_self");
            return true;
        }

        if (!(sender.hasPermission("lyttleessentials.top.other"))) {
            Message.sendMessage(sender, "no_permission");
            return true;
        }

        if (Bukkit.getPlayerExact(args[0]) == null) {
            Message.sendMessage(sender,"player_not_found");
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        String[][] replacementsSender = {{"<PLAYER>", getDisplayName(player)}};
        _teleportToTop(player);

        if (sender == Bukkit.getPlayerExact(args[0])) {
            Message.sendMessage(player, "top_self");
            return true;
        }

        if (sender instanceof Player) {
            String[][] replacementsPlayer = {{"<PLAYER>", getDisplayName((Player) sender)}};
            Message.sendMessage(sender, "top_other_sender", replacementsSender);
            Message.sendMessage(player, "top_other_player", replacementsPlayer);
            return true;
        }
        Message.sendMessage(sender,"top_other_sender", replacementsSender);
        Message.sendMessage(player, "top_console");
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
