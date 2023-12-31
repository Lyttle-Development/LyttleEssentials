package net.maplemc.lyttleessentials.commands;

import net.maplemc.lyttleessentials.LyttleEssentials;
import net.maplemc.lyttleessentials.types.Bill;
import net.maplemc.lyttleessentials.utils.Message;
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
            Message.sendConsole("must_be_player", Message.noReplacements);
            return true;
        }

        Player player = (Player) sender;

        if (Objects.equals(label, "sethome")) {
            if (args.length == 0) {
                Location location = player.getLocation();
                plugin.config.homes.set(player.getUniqueId().toString(), location);
                Message.sendPlayer(player, "sethome_success", Message.noReplacements);
                return true;
            }

            if (args.length == 1) {
                if (!player.hasPermission("lyttleessentials.sethome.other")) {
                    Message.sendPlayer(player, "no_permission", Message.noReplacements);
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

            Message.sendPlayer(player, "sethome_usage", Message.noReplacements);
            return true;
        }

        if (Objects.equals(label, "delhome")) {
            if (args.length == 0) {
                plugin.config.homes.set(player.getUniqueId().toString(), null);
                Message.sendPlayer(player, "delhome_success", Message.noReplacements);
                return true;
            }

            if (args.length == 1) {
                if (!player.hasPermission("lyttleessentials.delhome.other")) {
                    Message.sendPlayer(player, "no_permission", Message.noReplacements);
                    return true;
                }

                Player target = Bukkit.getPlayer(args[0]);

                if (target == null) {
                    Message.sendPlayer(player, "player_not_found", Message.noReplacements);
                    return true;
                }

                String homeName = args[0];
                plugin.config.homes.set(player.getName(), null);
                String[][] replacements = {{"<PLAYER>", homeName}};
                Message.sendPlayer(player, "delhome_other_success", replacements);
                return true;
            }

            Message.sendPlayer(player, "delhome_usage", Message.noReplacements);
            return true;
        }

        if (Objects.equals(label, "home")) {
            if (args.length == 0) {
                Location location = (Location) plugin.config.homes.get(player.getUniqueId().toString());
                if (location == null) {
                    Message.sendPlayer(player, "home_not_set", Message.noReplacements);
                    return true;
                }


                Bill bill = this.plugin.invoice.teleportToHome(player);
                if (bill.total == -1) {
                    Message.sendPlayer(player, "tokens_missing", Message.noReplacements);
                    return true;
                }

                player.teleport(location);
                String[][] replacements = {{"<PRICE>", String.valueOf(bill.total)}};
                Message.sendPlayer(player, "home_success", replacements);
                return true;
            }

            Message.sendPlayer(player, "home_usage", Message.noReplacements);
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        return Arrays.asList();
    }
}
