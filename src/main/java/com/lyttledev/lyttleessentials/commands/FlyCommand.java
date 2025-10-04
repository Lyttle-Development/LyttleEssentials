package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleutils.utils.selector.SelectorUtil;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class FlyCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    public FlyCommand(LyttleEssentials plugin) {
        this.plugin = plugin;
        plugin.getCommand("fly").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lyttleessentials.fly")) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        // /fly
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                plugin.message.sendMessage(sender, "fly_usage");
                return true;
            }
            if (!sender.hasPermission("lyttleessentials.fly.self")) {
                plugin.message.sendMessage(sender, "no_permission");
                return true;
            }
            boolean active = toggleFly((Player) sender);
            flySelfMessage((Player) sender, active);
            return true;
        }

        // /fly true|false (self)
        if (args.length == 1 && (args[0].equalsIgnoreCase("true") || args[0].equalsIgnoreCase("false"))) {
            if (!(sender instanceof Player)) {
                plugin.message.sendMessage(sender, "fly_usage");
                return true;
            }
            if (!sender.hasPermission("lyttleessentials.fly.self")) {
                plugin.message.sendMessage(sender, "no_permission");
                return true;
            }
            boolean enable = Boolean.parseBoolean(args[0]);
            Player self = (Player) sender;

            // No-op if already in requested state (avoid spam)
            if (self.getAllowFlight() == enable) {
                return true;
            }

            boolean active = setFly(self, enable);
            flySelfMessage(self, active);
            return true;
        }

        // /fly <selector> [true|false]
        if (!sender.hasPermission("lyttleessentials.fly.other")) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        List<Entity> targets = SelectorUtil.resolveSelector(sender, args[0], true);
        if (targets.isEmpty()) {
            plugin.message.sendMessage(sender, "player_not_found");
            return true;
        }

        Boolean setValue = null;
        if (args.length >= 2) {
            if (!(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false"))) {
                plugin.message.sendMessage(sender, "fly_usage");
                return true;
            }
            setValue = Boolean.parseBoolean(args[1]);
        } else if (args.length > 2) {
            plugin.message.sendMessage(sender, "fly_usage");
            return true;
        }

        for (Entity e : targets) {
            if (!(e instanceof Player)) continue;
            Player player = (Player) e;

            // Toggle if no explicit value, else set if different (avoid spam if same)
            boolean active;
            if (setValue == null) {
                active = toggleFly(player);
            } else {
                if (player.getAllowFlight() == setValue) {
                    // Already in requested state; skip messaging
                    continue;
                }
                active = setFly(player, setValue);
            }

            if (sender == player) {
                flySelfMessage((Player) sender, active);
                continue;
            }

            Replacements replacementsSender = new Replacements.Builder()
                    .add("<PLAYER>", getDisplayName(player))
                    .build();

            if (sender instanceof Player) {
                Replacements replacementsPlayer = new Replacements.Builder()
                        .add("<PLAYER>", getDisplayName((Player) sender))
                        .build();

                if (active) {
                    plugin.message.sendMessage(sender, "fly_activate_other_sender", replacementsSender);
                    plugin.message.sendMessage(player, "fly_activate_other_target", replacementsPlayer);
                    continue;
                }
                plugin.message.sendMessage(sender, "fly_deactivate_other_sender", replacementsSender);
                plugin.message.sendMessage(player, "fly_deactivate_other_target", replacementsPlayer);
                continue;
            }

            if (active) {
                plugin.message.sendMessage(sender, "fly_activate_other_sender", replacementsSender);
                plugin.message.sendMessage(player, "fly_activate_console");
                continue;
            }
            plugin.message.sendMessage(sender, "fly_deactivate_other_sender", replacementsSender);
            plugin.message.sendMessage(player, "fly_deactivate_console");
        }
        return true;
    }

    public boolean toggleFly(Player player) {
        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
            return false;
        }
        player.setAllowFlight(true);
        return true;
    }

    public boolean setFly(Player player, boolean enable) {
        player.setAllowFlight(enable);
        return enable;
    }

    public void flySelfMessage(Player receiver, boolean active) {
        if (active) {
            plugin.message.sendMessage(receiver, "fly_activate");
            return;
        }
        plugin.message.sendMessage(receiver, "fly_deactivate");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1) {
            return SelectorUtil.selectorCompletions(arguments[0]);
        }
        if (arguments.length == 2) {
            return List.of("true", "false");
        }
        return List.of();
    }
}