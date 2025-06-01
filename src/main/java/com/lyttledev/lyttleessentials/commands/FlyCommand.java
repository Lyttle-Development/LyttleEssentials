package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
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

        if (!(sender instanceof Player) && args.length != 1) {
            plugin.message.sendMessage(sender, "fly_usage");
            return true;
        }

        if (args.length > 1) {
            plugin.message.sendMessage(sender, "fly_usage");
            return true;
        }

        if (args.length == 0) {
            if (!sender.hasPermission("lyttleessentials.fly.self")) {
                plugin.message.sendMessage(sender, "no_permission");
                return true;
            }
            boolean active = toggleFly((Player) sender);
            flySelfMessage((Player) sender, active);
            return true;
        }

        if (!sender.hasPermission("lyttleessentials.fly.other")) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if ((Bukkit.getPlayerExact(args[0]) == null)) {
            plugin.message.sendMessage(sender, "player_not_found");
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        boolean active = toggleFly(player);

        if (sender == Bukkit.getPlayerExact(args[0])) {
            flySelfMessage((Player) sender, active);
            return true;
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
                return true;
            }
            plugin.message.sendMessage(sender, "fly_deactivate_other_sender", replacementsSender);
            plugin.message.sendMessage(player, "fly_deactivate_other_target", replacementsPlayer);
            return true;
        }

        if (active) {
            plugin.message.sendMessage(sender, "fly_activate_other_sender", replacementsSender);
            plugin.message.sendMessage(player, "fly_activate_console");
            return true;
        }
        plugin.message.sendMessage(sender, "fly_deactivate_other_sender", replacementsSender);
        plugin.message.sendMessage(player, "fly_deactivate_console");
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
            return null;
        }
        return List.of();
    }
}
