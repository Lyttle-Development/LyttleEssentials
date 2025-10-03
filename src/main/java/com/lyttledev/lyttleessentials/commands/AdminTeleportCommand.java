package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.SelectorUtil.SelectorUtil;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class AdminTeleportCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    public AdminTeleportCommand(LyttleEssentials plugin) {
        plugin.getCommand("atp").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.message.sendMessage(sender,"must_be_player");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lyttleessentials.admintp")) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if (args.length == 1) {
            if (!player.hasPermission("lyttleessentials.admintp.self")) {
                plugin.message.sendMessage(sender, "no_permission");
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

                player.teleport(target);

                Replacements replacements = new Replacements.Builder()
                        .add("<USER>", getDisplayName(player))
                        .add("<TARGET>", getDisplayName(target))
                        .build();

                plugin.message.sendMessage(player, "atp_user", replacements, player);
            }
            return true;
        }

        if (!player.hasPermission("lyttleessentials.admintp.other")) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if (args.length == 2) {
            List<Entity> users = SelectorUtil.resolveSelector(player, args[0], true);
            List<Entity> targets = SelectorUtil.resolveSelector(player, args[1], true);

            if (users.isEmpty() || targets.isEmpty()) {
                plugin.message.sendMessage(player, "player_not_found");
                return true;
            }

            for (Entity ue : users) {
                if (!(ue instanceof Player)) continue;
                Player user = (Player) ue;

                for (Entity te : targets) {
                    if (!(te instanceof Player)) continue;
                    Player target = (Player) te;

                    user.teleport(target);

                    Replacements replacements = new Replacements.Builder()
                            .add("<USER>", getDisplayName(user))
                            .add("<TARGET>", getDisplayName(target))
                            .build();

                    plugin.message.sendMessage(player, "atp_user", replacements, player);
                }
            }
            return true;
        }

        if (args.length == 3) {
            plugin.console.run(player, "minecraft:tp " + args[0] + " " + args[1] + " " + args[2]);
            return true;
        }

        if (args.length == 4) {
            List<Entity> users = SelectorUtil.resolveSelector(player, args[0], true);
            if (users.isEmpty()) {
                plugin.message.sendMessage(player, "player_not_found");
                return true;
            }

            for (Entity ue : users) {
                if (!(ue instanceof Player)) continue;
                Player user = (Player) ue;

                plugin.console.run("minecraft:execute as " +  getDisplayName(user) + " at @s run tp " + args[1] + " " + args[2] + " " + args[3]);

                Replacements replacements = new Replacements.Builder()
                        .add("<USER>", getDisplayName(user))
                        .add("<TARGET>", "Loc(" + args[1] + ", " + args[2] + ", " + args[3] + ")")
                        .build();

                plugin.message.sendMessage(player, "atp_user", replacements, player);
            }
            return true;
        }

        plugin.message.sendMessage(player, "atp_usage");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1 || arguments.length == 2) {
            return SelectorUtil.selectorCompletions(arguments[arguments.length - 1]);
        }
        return List.of();
    }
}