package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class FlyCommand implements CommandExecutor, TabCompleter {

    public FlyCommand(LyttleEssentials plugin) {
        plugin.getCommand("fly").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lyttleessentials.fly")) {
            Message.sendMessage(sender, "no_permission");
            return true;
        }

        if (!(sender instanceof Player) && args.length != 1) {
            Message.sendMessage(sender, "fly_usage");
            return true;
        }

        if (args.length > 1) {
            Message.sendMessage(sender, "fly_usage");
            return true;
        }

        if (args.length == 0) {
            if (!sender.hasPermission("lyttleessentials.fly.self")) {
                Message.sendMessage(sender, "no_permission");
                return true;
            }
            boolean active = toggleFly((Player) sender);
            flySelfMessage((Player) sender, active);
            return true;
        }

        if (!sender.hasPermission("lyttleessentials.fly.other")) {
            Message.sendMessage(sender, "no_permission");
            return true;
        }

        if ((Bukkit.getPlayerExact(args[0]) == null)) {
            Message.sendMessage(sender, "player_not_found");
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        boolean active = toggleFly(player);

        if (sender == Bukkit.getPlayerExact(args[0])) {
            flySelfMessage((Player) sender, active);
            return true;
        }

        String[][] replacementsSender = {{"<PLAYER>", getDisplayName(player)}};

        if (sender instanceof Player) {
            String[][] replacementsPlayer = {{"<PLAYER>", getDisplayName((Player) sender)}};
            if (active) {
                Message.sendMessage(sender, "fly_activate_other_sender", replacementsSender);
                Message.sendMessage(player, "fly_activate_other_target", replacementsPlayer);
                return true;
            }
            Message.sendMessage(sender, "fly_deactivate_other_sender", replacementsSender);
            Message.sendMessage(player, "fly_deactivate_other_target", replacementsPlayer);
            return true;
        }

        if (active) {
            Message.sendMessage(sender, "fly_activate_other_sender", replacementsSender);
            Message.sendMessage(player, "fly_activate_console");
            return true;
        }
        Message.sendMessage(sender, "fly_deactivate_other_sender", replacementsSender);
        Message.sendMessage(player, "fly_deactivate_console");
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
            Message.sendMessage(receiver, "fly_activate");
            return;
        }
        Message.sendMessage(receiver, "fly_deactivate");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1) {
            return null;
        }
        return List.of();
    }
}
