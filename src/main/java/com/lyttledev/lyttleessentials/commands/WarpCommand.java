package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.types.Bill;
import com.lyttledev.lyttleessentials.types.Warp;
import com.lyttledev.lyttleessentials.utils.MessageCleaner;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class WarpCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    public WarpCommand(LyttleEssentials plugin) {
        plugin.getCommand("warp").setExecutor(this);
        plugin.getCommand("setwarp").setExecutor(this);
        plugin.getCommand("delwarp").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.message.sendMessage(sender,"must_be_player");
            return true;
        }

        if (!(sender.hasPermission("lyttleessentials.warp"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        Player player = (Player) sender;

        if (Objects.equals(label, "setwarp")) {
            if (!(sender.hasPermission("lyttleessentials.warp.set"))) {
                plugin.message.sendMessage(sender, "no_permission");
                return true;
            }

            if (args.length == 0) {
                // No name provided
                plugin.message.sendMessage(player, "setwarp_no_name");
                return true;
            }

            if (args.length == 1) {
                // Name provided, for current player
                if (!(sender.hasPermission("lyttleessentials.warp.set.self"))) {
                    plugin.message.sendMessage(sender, "no_permission");
                    return true;
                }
                String warpName = MessageCleaner.cleanMessage(args[0]);
                Warp warp = new Warp(player, warpName);
                if (!plugin.config.warps.containsLowercase(warpName)) {
                    Bill bill = plugin.invoice.createWarp(player);
                    if (bill.total < 0) {
                        plugin.message.sendMessage(player, "tokens_missing");
                        return true;
                    }

                    plugin.config.warps.set(warpName, warp);
                    Replacements replacements = new Replacements.Builder()
                        .add("<NAME>", warpName)
                        .add("<PRICE>", String.valueOf(bill.total))
                        .build();

                    plugin.message.sendMessage(player, "setwarp_success", replacements);
                } else {
                    Replacements replacements = new Replacements.Builder()
                        .add("<NAME>", warpName)
                        .build();

                    plugin.message.sendMessage(player, "setwarp_already_exists", replacements);
                }
                return true;
            }

            if (args.length == 2) {
                // Name and player provided
                if (!(sender.hasPermission("lyttleessentials.warp.set.other"))) {
                    plugin.message.sendMessage(sender, "no_permission");
                    return true;
                }
                String warpName = args[0];
                Player target = Bukkit.getPlayer(args[1]);
                Warp warp = new Warp(target, warpName);
                Replacements replacements = new Replacements.Builder()
                    .add("<NAME>", warpName)
                    .add("<PLAYER>", getDisplayName(target))
                    .build();

                if (!plugin.config.warps.containsLowercase(warpName)) {
                    plugin.config.warps.set(warpName, warp);
                    plugin.message.sendMessage(player, "setwarp_success_other", replacements);
                } else {
                    plugin.message.sendMessage(player, "setwarp_already_exists_other", replacements);
                }
                return true;
            }
        }

        if (Objects.equals(label, "delwarp")) {
            if (!(sender.hasPermission("lyttleessentials.warp.del"))) {
                plugin.message.sendMessage(sender, "no_permission");
                return true;
            }

            if (args.length == 0) {
                // No name provided
                plugin.message.sendMessage(player, "delwarp_no_name");
                return true;
            }

            if (args.length == 1) {
                // Name provided
                if (player.hasPermission("lyttleessentials.del.warp.others") && args.length == 2) {
                    // Name and player provided
                    String warpName = args[0];
                    Player target = Bukkit.getPlayer(args[1]);

                    Replacements replacements = new Replacements.Builder()
                        .add("<NAME>", warpName)
                        .add("<PLAYER>", getDisplayName(target))
                        .build();

                    if (plugin.config.warps.contains(warpName)) {
                        plugin.config.warps.remove(warpName);
                        plugin.message.sendMessage(player, "delwarp_success_other", replacements);
                    } else {
                        plugin.message.sendMessage(player, "delwarp_doesnt_exist_other", replacements);
                    }
                }

                if (!(sender.hasPermission("lyttleessentials.warp.del.self"))) {
                    plugin.message.sendMessage(sender, "no_permission");
                    return true;
                }

                String warpName = args[0];
                Replacements replacements = new Replacements.Builder()
                    .add("<NAME>", warpName)
                    .build();

                if (plugin.config.warps.contains(warpName)) {
                    ConfigurationSection warpSection = plugin.config.warps.getSection(warpName);
                    if (warpSection == null) {
                        plugin.message.sendMessage(player, "warp_doesnt_exist");
                        return true;
                    }
                    Warp warp = new Warp(warpSection);

                    if (!Objects.equals(warp.owner, player.getUniqueId().toString())) {
                        plugin.message.sendMessage(player, "delwarp_not_owner", replacements);
                        return true;
                    }
                    plugin.config.warps.remove(warpName);

                    plugin.message.sendMessage(player, "delwarp_success", replacements);
                } else {
                    plugin.message.sendMessage(player, "delwarp_doesnt_exist", replacements);
                }
                return true;
            }
        }

        if (Objects.equals(label, "warp")) {
            if (args.length == 0) {
                // No name provided
                plugin.message.sendMessage(player, "warp_no_name");
                return true;
            }

            if (args.length == 1) {
                if (!(sender.hasPermission("lyttleessentials.warp.self"))) {
                    plugin.message.sendMessage(sender, "no_permission");
                    return true;
                }

                // Name provided
                String warpName = args[0];
                if (plugin.config.warps.contains(warpName)) {
                    ConfigurationSection warpSection = plugin.config.warps.getSection(warpName);
                    if (warpSection == null) {
                        plugin.message.sendMessage(player, "warp_doesnt_exist");
                        return true;
                    }
                    Warp warp = new Warp(warpSection);

                    Location location = warp.location;

                    Bill bill = plugin.invoice.teleportToWarp(player);
                    if (bill.total < 0) {
                        plugin.message.sendMessage(player, "tokens_missing");
                        return true;
                    }

                    player.teleport(location);
                    Replacements replacements = new Replacements.Builder()
                            .add("<NAME>", warpName)
                            .add("<PRICE>", String.valueOf(bill.total))
                            .build();

                    plugin.message.sendMessage(player, "warp_success", replacements);
                } else {
                    Replacements replacements = new Replacements.Builder()
                            .add("<NAME>", warpName)
                            .build();
                    plugin.message.sendMessage(player, "warp_doesnt_exist", replacements);
                }
                return true;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1) {
            String[] warps = plugin.config.warps.getKeys("");
            return Arrays.asList(warps);
        }

        if (sender.hasPermission("lyttleessentials.warp.others")) {
            if (arguments.length == 2) {
                return null;
            }
        }

        return List.of();
    }
}