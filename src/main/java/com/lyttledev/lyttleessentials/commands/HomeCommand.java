package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.types.Bill;
import com.lyttledev.lyttleutils.utils.selector.SelectorUtil;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Bukkit;
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

            // /sethome (self)
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

            // /sethome <selector> (set home for others via selector)
            // Requires either legacy permission or the new extra permission "lyttleessentials.sethome.other"
            boolean hasLegacy = player.hasPermission("lyttleessentials.home.set.other");
            boolean hasNew = player.hasPermission("lyttleessentials.sethome.other");
            if (!hasLegacy && !hasNew) {
                plugin.message.sendMessage(player, "no_permission");
                return true;
            }

            List<Entity> targets = SelectorUtil.resolveSelector(player, args[0], true);
            if (targets.isEmpty()) {
                plugin.message.sendMessage(player, "player_not_found");
                return true;
            }

            Location location = player.getLocation();

            for (Entity e : targets) {
                if (!(e instanceof Player)) continue;
                Player t = (Player) e;

                plugin.config.homes.set(t.getUniqueId().toString(), location);

                Replacements replacements = new Replacements.Builder()
                        .add("<PLAYER>", getDisplayName(t))
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

            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                plugin.message.sendMessage(player, "player_not_found");
                return true;
            }

            String homeName = args[0];
            plugin.config.homes.set(target.getUniqueId().toString(), null);

            Replacements replacements = new Replacements.Builder()
                    .add("<PLAYER>", homeName)
                    .build();

            plugin.message.sendMessage(player, "delhome_other_success", replacements);
            return true;
        }

        if (Objects.equals(label, "home")) {

            // /home (self)
            if (args.length == 0) {
                if (!player.hasPermission("lyttleessentials.home.self")) {
                    plugin.message.sendMessage(player, "no_permission");
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

            // /home <owner>  or  /home <owner> <selector>
            if (!player.hasPermission("lyttleessentials.home.other")) {
                plugin.message.sendMessage(player, "no_permission");
                return true;
            }

            Player owner = Bukkit.getPlayer(args[0]);
            if (owner == null) {
                plugin.message.sendMessage(player, "player_not_found");
                return true;
            }

            Location ownerHome = (Location) plugin.config.homes.get(owner.getUniqueId().toString());
            if (ownerHome == null) {
                Replacements repl = new Replacements.Builder()
                        .add("<PLAYER>", getDisplayName(owner))
                        .build();
                plugin.message.sendMessage(player, "home_other_not_set", repl);
                return true;
            }

            // /home <owner>
            if (args.length == 1) {
                // executor teleports self to owner's home (executor pays)
                Bill bill = this.plugin.invoice.teleportToHome(player);
                if (bill.total == -1) {
                    plugin.message.sendMessage(player, "tokens_missing");
                    return true;
                }

                player.teleport(ownerHome);
                Replacements repl = new Replacements.Builder()
                        .add("<PLAYER>", getDisplayName(owner))
                        .add("<PRICE>", String.valueOf(bill.total))
                        .build();
                plugin.message.sendMessage(player, "home_other_success", repl);
                return true;
            }

            // /home <owner> <selector>  (executor pays for each target)
            List<Entity> targets = SelectorUtil.resolveSelector(player, args[1], true);
            if (targets.isEmpty()) {
                plugin.message.sendMessage(player, "player_not_found");
                return true;
            }

            for (Entity e : targets) {
                if (!(e instanceof Player)) continue;
                Player t = (Player) e;

                Bill bill = this.plugin.invoice.teleportToHome(player);
                if (bill.total == -1) {
                    plugin.message.sendMessage(player, "tokens_missing");
                    return true;
                }

                t.teleport(ownerHome);

                Replacements rSender = new Replacements.Builder()
                        .add("<OWNER>", getDisplayName(owner))
                        .add("<TARGET>", getDisplayName(t))
                        .add("<PRICE>", String.valueOf(bill.total))
                        .build();
                Replacements rTarget = new Replacements.Builder()
                        .add("<PLAYER>", getDisplayName(player))
                        .add("<OWNER>", getDisplayName(owner))
                        .build();

                plugin.message.sendMessage(player, "home_other_sender", rSender);
                plugin.message.sendMessage(t, "home_other_target", rTarget);
            }
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (command.getName().equalsIgnoreCase("home")) {
            if (arguments.length == 1) {
                // suggest online players as owner
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
            if (arguments.length == 2) {
                // suggest selector for targets
                return SelectorUtil.selectorCompletions(arguments[1]);
            }
        }

        if (command.getName().equalsIgnoreCase("sethome")) {
            if (arguments.length == 1) {
                return SelectorUtil.selectorCompletions(arguments[0]);
            }
        }

        return List.of();
    }
}