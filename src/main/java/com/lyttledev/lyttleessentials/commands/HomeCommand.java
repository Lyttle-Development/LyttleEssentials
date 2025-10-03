package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.types.Bill;
import com.lyttledev.lyttleessentials.utils.SelectorUtil.SelectorUtil;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
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

            List<Entity> targets = SelectorUtil.resolveSelector(player, args[0], true);

            if (targets.isEmpty()) {
                Replacements replacements = new Replacements.Builder()
                        .add("%player%", args[0])
                        .build();

                plugin.message.sendMessage(player, "player_not_found", replacements);
                return true;
            }

            Location location = player.getLocation();
            for (Entity e : targets) {
                if (!(e instanceof Player)) continue;
                Player target = (Player) e;

                plugin.config.homes.set(target.getUniqueId().toString(), location);

                Replacements replacements = new Replacements.Builder()
                        .add("<PLAYER>", getDisplayName(target))
                        .build();

                plugin.message.sendMessage(player, "sethome_other_success", replacements);
            }
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

            List<Entity> targets = SelectorUtil.resolveSelector(player, args[0], true);

            if (targets.isEmpty()) {
                plugin.message.sendMessage(player, "player_not_found");
                return true;
            }

            for (Entity e : targets) {
                if (!(e instanceof Player)) continue;
                Player target = (Player) e;

                plugin.config.homes.set(target.getUniqueId().toString(), null);

                Replacements replacements = new Replacements.Builder()
                        .add("<PLAYER>", getDisplayName(target))
                        .build();

                plugin.message.sendMessage(player, "delhome_other_success", replacements);
            }
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
        String name = command.getName().toLowerCase();
        if ((name.equals("sethome") || name.equals("delhome")) && arguments.length == 1) {
            return SelectorUtil.selectorCompletions(arguments[0]);
        }
        return List.of();
    }
}