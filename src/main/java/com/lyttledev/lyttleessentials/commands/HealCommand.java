package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.attribute.Attribute;
import java.util.List;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class HealCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    public HealCommand(LyttleEssentials plugin) {
        this.plugin = plugin;
        plugin.getCommand("heal").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lyttleessentials.heal")) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if (args.length > 1) {
            plugin.message.sendMessage(sender,"heal_usage");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                plugin.message.sendMessage(sender,"heal_usage");
                return true;
            }
            if (!sender.hasPermission("lyttleessentials.heal.self")) {
                plugin.message.sendMessage(sender, "no_permission");
                return true;
            }
            plugin.message.sendMessage(sender, "heal_self");
            heal((Player) sender);
            return true;
        }

        if (!(sender.hasPermission("lyttleessentials.heal.other"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if ((Bukkit.getPlayerExact(args[0]) == null)) {
            plugin.message.sendMessage(sender,"player_not_found");
            return true;
        }

        Player player = Bukkit.getPlayerExact(args[0]);
        heal(player);

        if (sender == Bukkit.getPlayerExact(args[0])) {
            healSelfMessage((Player) sender);
            return true;
        }

        Replacements replacementsSender = new Replacements.Builder()
            .add("<PLAYER>", getDisplayName(player))
            .build();

        if (sender instanceof Player) {
            Replacements replacementsPlayer = new Replacements.Builder()
                .add("<PLAYER>", getDisplayName((Player) sender))
                .build();

            plugin.message.sendMessage(player, "heal_other_player", replacementsPlayer);
            plugin.message.sendMessage(sender, "heal_other_sender", replacementsSender);
            return true;
        }

        plugin.message.sendMessage(player, "heal_console");
        plugin.message.sendMessage(sender,"heal_other_sender", replacementsSender);
        return true;
    }

    private void heal(Player player) {
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        for (PotionEffect effect : player.getActivePotionEffects()) { player.removePotionEffect(effect.getType()); }
    }

    private void healSelfMessage(Player player) {
        plugin.message.sendMessage(player, "heal_self");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1) {
            return null;
        }
        return List.of();
    }
}
