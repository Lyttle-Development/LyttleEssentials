package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class RepairCommand implements CommandExecutor, TabCompleter {

    public RepairCommand(LyttleEssentials plugin) {
        plugin.getCommand("repair").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender.hasPermission("lyttleessentials.repair"))) {
            Message.sendMessage(sender, "no_permission");
            return true;
        }

        if (args.length > 2 || args.length == 0) {
            Message.sendMessage(sender,"repair_usage");
            return true;
        }

        if (args.length == 1) {
            if (!(sender.hasPermission("lyttleessentials.repair.self"))) {
                Message.sendMessage(sender, "no_permission");
                return true;
            }

            if (!(sender instanceof Player)) {
                Message.sendMessage(sender,"repair_usage");
                return true;
            }

            Player player = (Player) sender;

            if (args[0].equalsIgnoreCase("HeldItem")) {
                _repairItem(player.getInventory().getItemInMainHand());
                Message.sendMessage(player, "repair_helditem_self");
                return true;
            }

            _repairInventory(player.getInventory());
            Message.sendMessage(player, "repair_all_self");
            return true;
        }

        if (!(sender.hasPermission("lyttleessentials.repair.other"))) {
            Message.sendMessage(sender, "no_permission");
            return true;
        }

        if (Bukkit.getPlayerExact(args[1]) == null) {
            Message.sendMessage(sender,"player_not_found");
            return true;
        }

        Player player = Bukkit.getPlayerExact(args[1]);

        String[][] replacementsSender = {{"<PLAYER>", getDisplayName(player)}};

        if (args[0].equalsIgnoreCase("HeldItem")) {
            _repairItem(player.getInventory().getItemInMainHand());
            if (sender instanceof Player) {
                String[][] replacementsPlayer = {{"<PLAYER>", getDisplayName((Player) sender)}};
                Message.sendMessage(player, "repair_helditem_other_player", replacementsPlayer);
                Message.sendMessage(sender, "repair_helditem_other_sender", replacementsSender);
                return true;
            }
            Message.sendMessage(player, "repair_helditem_other_console");
            Message.sendMessage(sender,"repair_helditem_other_sender", replacementsSender);
            return true;
        }
        _repairInventory(player.getInventory());
        if (sender instanceof Player) {
            String[][] replacementsPlayer = {{"<PLAYER>", getDisplayName((Player) sender)}};
            Message.sendMessage(player, "repair_all_other_player", replacementsPlayer);
            Message.sendMessage(sender, "repair_all_other_sender", replacementsSender);
            return true;
        }
        Message.sendMessage(player, "repair_all_other_console");
        Message.sendMessage(sender,"repair_all_other_sender", replacementsSender);
        return true;
    }

    private void _repairItem(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof Damageable) {
            ((Damageable) itemMeta).setDamage(0);
            item.setItemMeta(itemMeta);
        }
    }

    private void _repairInventory(PlayerInventory inventory) {
        for (ItemStack item : inventory) {
            if (item == null) continue;
            if (!item.hasItemMeta()) continue;
            _repairItem(item);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = Arrays.asList("HeldItem", "all");
            List<String> result = new ArrayList<>(Collections.emptyList());
            for (String option : options) {
                if (option.toLowerCase().startsWith(args[0].toLowerCase())) {
                    result.add(option);
                }
            }
            return result;
        }

        if (args.length == 2) { return null; }

        return List.of();
    }
}
