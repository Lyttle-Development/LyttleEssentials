package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Arrays;
import java.util.List;

public class HealCommand implements CommandExecutor, TabCompleter {
    private LyttleEssentials plugin;

    public HealCommand(LyttleEssentials plugin) {
        plugin.getCommand("heal").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 1) {
            if (!sender.hasPermission("lyttleessentials.heal")) {
                Message.sendPlayer((Player) sender, "no_permission");
                return true;
            }
            if (sender instanceof Player) {
                Message.sendPlayer((Player) sender, "heal_usage");
                return true;
            }
            Message.sendConsole("heal_usage");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                Message.sendConsole("heal_usage");
                return true;
            }
            if (!sender.hasPermission("lyttleessentials.heal.self")) {
                Message.sendPlayer((Player) sender, "no_permission");
                return true;
            }
            Message.sendPlayer((Player) sender, "heal_self");
            heal((Player) sender);
            return true;
        }

        if (!(sender.hasPermission("lyttleessentials.heal.other"))) {
            Message.sendPlayer((Player) sender, "no_permission");
            return true;
        }

        if ((Bukkit.getPlayerExact(args[0]) == null)) {
            if (sender instanceof Player) {
                Message.sendPlayer((Player) sender, "player_not_found");
                return true;
            }
            Message.sendConsole("player_not_found");
            return true;
        }

        Player player = Bukkit.getPlayerExact(args[0]);
        assert player != null;

        heal(player);

        if (sender == Bukkit.getPlayerExact(args[0])) {
            healSelfMessage((Player) sender);
            return true;
        }

        String[][] replacementsSender = {{"<PLAYER>", player.getDisplayName()}};

        if (sender instanceof Player) {
            String[][] replacementsPlayer = {{"<PLAYER>", ((Player) sender).getDisplayName()}};
            Message.sendPlayer(player, "heal_other_player", replacementsPlayer);
            Message.sendPlayer((Player) sender, "heal_other_sender", replacementsSender);
            return true;
        }

        Message.sendPlayer(player, "heal_console");
        Message.sendConsole("heal_other_sender", replacementsSender);
        return true;
    }

    private void heal(Player player) {
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        for (PotionEffect effect : player.getActivePotionEffects()) { player.removePotionEffect(effect.getType()); }
    }

    private void healSelfMessage(Player player) {
        Message.sendPlayer(player, "heal_self");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1) {
            return null;
        }
        return Arrays.asList();
    }
}
