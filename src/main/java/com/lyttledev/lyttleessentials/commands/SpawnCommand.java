package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.types.Bill;
import com.lyttledev.lyttleutils.utils.selector.SelectorUtil;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
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

        // /spawn (self)
        if (label.equalsIgnoreCase("spawn") && args.length == 0) {
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

        // /spawn <selector>  (executor pays)
        if (label.equalsIgnoreCase("spawn") && args.length == 1) {
            List<Entity> targets = SelectorUtil.resolveSelector(player, args[0], true);
            if (targets.isEmpty()) {
                plugin.message.sendMessage(player, "player_not_found");
                return true;
            }

            for (Entity e : targets) {
                if (!(e instanceof Player)) continue;
                Player t = (Player) e;

                Bill bill = plugin.invoice.teleportToSpawn(player);
                if (bill.total < 0) {
                    plugin.message.sendMessage(player, "tokens_missing");
                    return true;
                }

                t.teleport(spawn);

                Replacements rSender = new Replacements.Builder()
                        .add("<TARGET>", t.getName())
                        .add("<PRICE>", String.valueOf(bill.total))
                        .build();
                Replacements rTarget = new Replacements.Builder()
                        .add("<PLAYER>", player.getName())
                        .build();

                plugin.message.sendMessage(player, "spawn_teleported_other_sender", rSender);
                plugin.message.sendMessage(t, "spawn_teleported_target", rTarget);
            }
            return true;
        }

        plugin.message.sendMessage(player, "spawn_usage");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1) {
            return SelectorUtil.selectorCompletions(arguments[0]);
        }
        return List.of();
    }
}