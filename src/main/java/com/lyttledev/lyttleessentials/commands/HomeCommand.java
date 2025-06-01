package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.types.Bill;
import com.lyttledev.lyttleutils.types.Message.Replacements;
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
            plugin.message.sendMessage(sender,"must_be_player");
            return true;
        }

        if (!(sender.hasPermission("lyttleessentials.home"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        Player player = (Player) sender;

        if (Objects.equals(label, "sethome")) {
            if (!(sender.hasPermission("lyttleessentials.home.set"))) {
                plugin.message.sendMessage(sender, "no_permission");
                return true;
            }

            if (args.length == 0) {
                if (!(sender.hasPermission("lyttleessentials.home.set.self"))) {
                    plugin.message.sendMessage(sender, "no_permission");
                    return true;
                }
                Location location = player.getLocation();
                plugin.config.homes.set(player.getUniqueId().toString(), location);
                plugin.message.sendMessage(player, "sethome_success");
                return true;
            }

            if (!player.hasPermission("lyttleessentials.home.set.other")) {
                plugin.message.sendMessage(player, "no_permission");
                return true;
            }

            if (args.length != 1) {
                plugin.message.sendMessage(player, "sethome_usage");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                Replacements replacements = new Replacements.Builder()
                    .add("%player%", args[0])
                    .build();

                plugin.message.sendMessage(player, "player_not_found", replacements);
                return true;
            }

            String homeName = args[0];
            Location location = player.getLocation();
            plugin.config.homes.set(player.getUniqueId().toString(), location);

            Replacements replacements = new Replacements.Builder()
                .add("<PLAYER>", homeName)
                .build();

            plugin.message.sendMessage(player, "sethome_other_success", replacements);
            return true;
        }

        if (Objects.equals(label, "delhome")) {
            if (!(sender.hasPermission("lyttleessentials.home.del"))) {
                plugin.message.sendMessage(sender, "no_permission");
                return true;
            }

            if (args.length == 0) {
                if (!(sender.hasPermission("lyttleessentials.home.del.self"))) {
                    plugin.message.sendMessage(sender, "no_permission");
                    return true;
                }
                plugin.config.homes.set(player.getUniqueId().toString(), null);
                plugin.message.sendMessage(player, "delhome_success");
                return true;
            }

            if (!player.hasPermission("lyttleessentials.home.del.other")) {
                plugin.message.sendMessage(player, "no_permission");
                return true;
            }

            if (args.length != 1) {
                plugin.message.sendMessage(player, "delhome_usage");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                plugin.message.sendMessage(player, "player_not_found");
                return true;
            }

            String homeName = args[0];
            plugin.config.homes.set(getDisplayName(player), null);

            Replacements replacements = new Replacements.Builder()
                    .add("<PLAYER>", homeName)
                    .build();

            plugin.message.sendMessage(player, "delhome_other_success", replacements);
            return true;
        }

        if (Objects.equals(label, "home")) {

            if (!player.hasPermission("lyttleessentials.home.self")) {
                plugin.message.sendMessage(player, "no_permission");
                return true;
            }

            if (args.length != 0) {
                plugin.message.sendMessage(player, "home_usage");
                return true;
            }

            Location location = (Location) plugin.config.homes.get(player.getUniqueId().toString());
            if (location == null) {
                plugin.message.sendMessage(player, "home_not_set");
                return true;
            }

            Bill bill = this.plugin.invoice.teleportToHome(player);
            if (bill.total == -1) {
                plugin.message.sendMessage(player, "tokens_missing");
                return true;
            }

            player.teleport(location);
            Replacements replacements = new Replacements.Builder()
                .add("<PRICE>", String.valueOf(bill.total))
                .build();

            plugin.message.sendMessage(player, "home_success", replacements);
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        return List.of();
    }
}
