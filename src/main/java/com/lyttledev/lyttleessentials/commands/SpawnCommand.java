package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.SelectorUtil.SelectorUtil;
import com.lyttledev.lyttleessentials.types.Bill;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class SpawnCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    public SpawnCommand(LyttleEssentials plugin) {
        plugin.getCommand("spawn").setExecutor(this);
        plugin.getCommand("setspawn").setExecutor(this);
        plugin.getCommand("delspawn").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.message.sendMessage(sender,"must_be_player");
            return true;
        }

        Player player = (Player) sender;

        if (player.hasPermission("lyttleessentials.spawn.set")) {
            if (label.equalsIgnoreCase("setspawn")) {
                Location spawn = player.getLocation();
                plugin.config.locations.set("spawn", spawn);
                plugin.console.run(player, "setworldspawn");
                plugin.message.sendMessage(player, "spawn_set");
                return true;
            }
            if (label.equalsIgnoreCase("delspawn")) {
                plugin.config.locations.set("spawn", null);
                plugin.message.sendMessage(player, "spawn_deleted");
                return true;
            }
        }

        if (!(sender.hasPermission("lyttleessentials.spawn"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if (plugin.config.locations.get("spawn") == null) {
            plugin.message.sendMessage(player, "spawn_not_set");
            return true;
        }

        Location spawn = (Location) plugin.config.locations.get("spawn");

        if (args.length == 0) {
            Bill bill = plugin.invoice.teleportToSpawn(player);
            if (bill.total < 0) {
                plugin.message.sendMessage(player, "tokens_missing");
                return true;
            }

            player.teleport(spawn);
            Replacements replacements = new Replacements.Builder()
                    .add("<PRICE>", String.valueOf(bill.total))
                    .build();

            plugin.message.sendMessage(player, "spawn_teleported", replacements);
            return true;
        }

        if (!sender.hasPermission("lyttleessentials.spawn.other")) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        List<org.bukkit.entity.Entity> targets = SelectorUtil.resolveSelector(sender, args[0], true);
        if (targets.isEmpty()) {
            plugin.message.sendMessage(sender, "player_not_found");
            return true;
        }

        for (org.bukkit.entity.Entity e : targets) {
            if (!(e instanceof Player)) continue;
            Player target = (Player) e;

            Bill bill = plugin.invoice.teleportToSpawn(target);
            if (bill.total < 0) {
                plugin.message.sendMessage(target, "tokens_missing");
                continue;
            }

            target.teleport(spawn);
            Replacements replacements = new Replacements.Builder()
                    .add("<PRICE>", String.valueOf(bill.total))
                    .build();

            plugin.message.sendMessage(target, "spawn_teleported", replacements);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (command.getName().equalsIgnoreCase("spawn") && arguments.length == 1) {
            return SelectorUtil.selectorCompletions(arguments[0]);
        }
        return List.of();
    }
}