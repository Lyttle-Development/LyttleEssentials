package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.types.Bill;
import com.lyttledev.lyttleessentials.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class HomeCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    public HomeCommand(LyttleEssentials plugin) {
        plugin.getCommand("home").setExecutor(this);
        plugin.getCommand("sethome").setExecutor(this);
        plugin.getCommand("delhome").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Message.sendMessage(sender,"must_be_player");
            return true;
        }

        if (!(sender.hasPermission("lyttleessentials.home"))) {
            Message.sendMessage(sender, "no_permission");
            return true;
        }

        Player player = (Player) sender;

        if (Objects.equals(label, "sethome")) {
            if (!(sender.hasPermission("lyttleessentials.home.set"))) {
                Message.sendMessage(sender, "no_permission");
                return true;
            }

            if (args.length == 0) {
                if (!(sender.hasPermission("lyttleessentials.home.set.self"))) {
                    Message.sendMessage(sender, "no_permission");
                    return true;
                }
                Location location = player.getLocation();
                plugin.config.homes.set(player.getUniqueId().toString(), location);
                Message.sendMessage(player, "sethome_success");
                return true;
            }

            if (!player.hasPermission("lyttleessentials.home.set.other")) {
                Message.sendMessage(player, "no_permission");
                return true;
            }

            if (args.length != 1) {
                Message.sendMessage(player, "sethome_usage");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                String[][] replacements = {{"%player%", args[0]}};
                Message.sendMessage(player, "player_not_found", replacements);
                return true;
            }

            String homeName = args[0];
            Location location = player.getLocation();
            plugin.config.homes.set(player.getUniqueId().toString(), location);
            String[][] replacements = {{"<PLAYER>", homeName}};
            Message.sendMessage(player, "sethome_other_success", replacements);
            return true;
        }

        if (Objects.equals(label, "delhome")) {
            if (!(sender.hasPermission("lyttleessentials.home.del"))) {
                Message.sendMessage(sender, "no_permission");
                return true;
            }

            if (args.length == 0) {
                if (!(sender.hasPermission("lyttleessentials.home.del.self"))) {
                    Message.sendMessage(sender, "no_permission");
                    return true;
                }
                plugin.config.homes.set(player.getUniqueId().toString(), null);
                Message.sendMessage(player, "delhome_success");
                return true;
            }

            if (!player.hasPermission("lyttleessentials.home.del.other")) {
                Message.sendMessage(player, "no_permission");
                return true;
            }

            if (args.length != 1) {
                Message.sendMessage(player, "delhome_usage");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                Message.sendMessage(player, "player_not_found");
                return true;
            }

            String homeName = args[0];
            plugin.config.homes.set(getDisplayName(player), null);
            String[][] replacements = {{"<PLAYER>", homeName}};
            Message.sendMessage(player, "delhome_other_success", replacements);
            return true;
        }

        if (Objects.equals(label, "home")) {

            if (!player.hasPermission("lyttleessentials.home.self")) {
                Message.sendMessage(player, "no_permission");
                return true;
            }

            if (args.length != 0) {
                Message.sendMessage(player, "home_usage");
                return true;
            }

            Location location = (Location) plugin.config.homes.get(player.getUniqueId().toString());
            if (location == null) {
                Message.sendMessage(player, "home_not_set");
                return true;
            }

            Bill bill = this.plugin.invoice.teleportToHome(player);
            if (bill.total == -1) {
                Message.sendMessage(player, "tokens_missing");
                return true;
            }

            player.teleport(location);
            String[][] replacements = {{"<PRICE>", String.valueOf(bill.total)}};
            Message.sendMessage(player, "home_success", replacements);
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        return List.of();
    }
}
