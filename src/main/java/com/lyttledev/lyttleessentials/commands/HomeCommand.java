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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HomeCommand implements CommandExecutor, TabCompleter {
    private LyttleEssentials plugin;

    public HomeCommand(LyttleEssentials plugin) {
        plugin.getCommand("home").setExecutor(this);
        plugin.getCommand("sethome").setExecutor(this);
        plugin.getCommand("delhome").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Message.sendConsole("must_be_player");
            return true;
        }

        if (!(sender.hasPermission("lyttleessentials.home"))) {
            Message.sendPlayer((Player) sender, "no_permission");
            return true;
        }

        Player player = (Player) sender;

        if (Objects.equals(label, "sethome")) {
            if (!(sender.hasPermission("lyttleessentials.home.set"))) {
                Message.sendPlayer((Player) sender, "no_permission");
                return true;
            }

            if (args.length == 0) {
                Location location = player.getLocation();
                plugin.config.homes.set(player.getUniqueId().toString(), location);
                Message.sendPlayer(player, "sethome_success");
                return true;
            }

            if (args.length == 1) {
                if (!player.hasPermission("lyttleessentials.home.set.other")) {
                    Message.sendPlayer(player, "no_permission");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[0]);

                if (target == null) {
                    String[][] replacements = {{"%player%", args[0]}};
                    Message.sendPlayer(player, "player_not_found", replacements);
                    return true;
                }

                String homeName = args[0];
                Location location = player.getLocation();
                plugin.config.homes.set(player.getUniqueId().toString(), location);
                String[][] replacements = {{"<PLAYER>", homeName}};
                Message.sendPlayer(player, "sethome_other_success", replacements);
                return true;
            }

            Message.sendPlayer(player, "sethome_usage");
            return true;
        }

        if (Objects.equals(label, "delhome")) {
            if (!(sender.hasPermission("lyttleessentials.home.del"))) {
                Message.sendPlayer((Player) sender, "no_permission");
                return true;
            }

            if (args.length == 0) {
                plugin.config.homes.set(player.getUniqueId().toString(), null);
                Message.sendPlayer(player, "delhome_success");
                return true;
            }

            if (args.length == 1) {
                if (!player.hasPermission("lyttleessentials.home.del.other")) {
                    Message.sendPlayer(player, "no_permission");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[0]);

                if (target == null) {
                    Message.sendPlayer(player, "player_not_found");
                    return true;
                }

                String homeName = args[0];
                plugin.config.homes.set(player.getName(), null);
                String[][] replacements = {{"<PLAYER>", homeName}};
                Message.sendPlayer(player, "delhome_other_success", replacements);
                return true;
            }

            Message.sendPlayer(player, "delhome_usage");
            return true;
        }

        if (Objects.equals(label, "home")) {
            if (args.length == 0) {
                Location location = (Location) plugin.config.homes.get(player.getUniqueId().toString());
                if (location == null) {
                    Message.sendPlayer(player, "home_not_set");
                    return true;
                }


                Bill bill = this.plugin.invoice.teleportToHome(player);
                if (bill.total == -1) {
                    Message.sendPlayer(player, "tokens_missing");
                    return true;
                }

                player.teleport(location);
                String[][] replacements = {{"<PRICE>", String.valueOf(bill.total)}};
                Message.sendPlayer(player, "home_success", replacements);
                return true;
            }

            Message.sendPlayer(player, "home_usage");
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        return Arrays.asList();
    }
}
