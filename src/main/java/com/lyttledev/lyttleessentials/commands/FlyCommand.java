package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class FlyCommand implements CommandExecutor, TabCompleter {
    private LyttleEssentials plugin;

    public FlyCommand(LyttleEssentials plugin) {
        plugin.getCommand("fly").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && args.length != 1) {
            Message.sendConsole("fly_usage", Message.noReplacements);
            return true;
        }

        if (args.length > 1) {
            Message.sendPlayer((Player) sender, "fly_usage", Message.noReplacements);
            return true;
        }

        if (args.length == 0) {
            boolean active = toggleFly((Player) sender);
            flySelfMessage((Player) sender, active);
            return true;
        }

        if ((Bukkit.getPlayerExact(args[0]) == null)) {
            if (sender instanceof Player) {
                Message.sendPlayer((Player) sender, "player_not_found", Message.noReplacements);
                return true;
            }
            Message.sendConsole("player_not_found", Message.noReplacements);
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        boolean active = toggleFly(player);

        if (sender == Bukkit.getPlayerExact(args[0])) {
            flySelfMessage((Player) sender, active);
            return true;
        }

        String[][] replacementsSender = {{"<PLAYER>", player.getDisplayName()}};

        if (sender instanceof Player) {
            String[][] replacementsPlayer = {{"<PLAYER>", ((Player) sender).getDisplayName()}};
            if (active) {
                Message.sendPlayer((Player) sender, "fly_activate_other_sender", replacementsSender);
                Message.sendPlayer(player, "fly_activate_other_target", replacementsPlayer);
                return true;
            }
            Message.sendPlayer((Player) sender, "fly_deactivate_other_sender", replacementsSender);
            Message.sendPlayer(player, "fly_deactivate_other_target", replacementsPlayer);
            return true;
        }

        if (active) {
            Message.sendConsole("fly_activate_other_sender", replacementsSender);
            Message.sendPlayer(player, "fly_activate_console", Message.noReplacements);
            return true;
        }
        Message.sendConsole("fly_deactivate_other_sender", replacementsSender);
        Message.sendPlayer(player, "fly_deactivate_console", Message.noReplacements);
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
            Message.sendPlayer(receiver, "fly_activate", Message.noReplacements);
            return;
        }
        Message.sendPlayer(receiver, "fly_deactivate", Message.noReplacements);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1) {
            return null;
        }
        return Arrays.asList();
    }
}
