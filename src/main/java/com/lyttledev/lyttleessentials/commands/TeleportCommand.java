package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.types.Bill;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;

import java.util.*;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class TeleportCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    static HashMap<UUID, UUID> targetMap = new HashMap<>();

    public TeleportCommand(LyttleEssentials plugin) {
        plugin.getCommand("tp").setExecutor(this);
        plugin.getCommand("tpaccept").setExecutor(this);
        plugin.getCommand("tpdeny").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.message.sendMessage(sender, "must_be_player");
            return true;
        }

        if (!(sender.hasPermission("lyttleessentials.tp"))) {
            plugin.message.sendMessage(sender, "no_permission");
            return true;
        }

        if (label.equalsIgnoreCase("tp")) {
            return tpCommand(sender, command, label, args);
        }

        if (label.equalsIgnoreCase("tpaccept")) {
            return tpAcceptCommand(sender, command, label, args);
        }

        if (label.equalsIgnoreCase("tpdeny")) {
            return tpDenyCommand(sender, command, label, args);
        }

        return true;
    }

    public boolean tpCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        // Check if target player is online and found
        if (Bukkit.getPlayerExact(args[0]) == null) {
            plugin.message.sendMessage(player, "player_not_found");
            return true;
        }

        // Stores some crucial info
        Player playerTarget = Bukkit.getPlayer(args[0]);

        // Checks for a TP to the player himself
        if (player == playerTarget) {
            plugin.message.sendMessage(player, "tp_self");
            return true;
        }

        // Check if the player already has a /tp request
        if (targetMap.containsKey(player.getUniqueId())) {
            plugin.message.sendMessage(player, "tp_already_requested");
            return true;
        }

        // send a message if the player does not have enough tokens
        Bill bill = plugin.invoice.teleportToPlayerCheck(player);
        if (bill.total < 0) {
            Replacements replacements = new Replacements.Builder()
                    .add("<TOKENS>", String.valueOf(bill.total))
                    .build();

            plugin.message.sendMessage(player, "tokens_missing_amount", replacements);
            return true;
        }

        Replacements replacements = new Replacements.Builder()
            .add("<PLAYER>", getDisplayName(player))
            .build();

        plugin.message.sendMessage(playerTarget, "tp_ask_target", replacements);

        targetMap.put(player.getUniqueId(), playerTarget.getUniqueId());

        Replacements replacements2 = new Replacements.Builder()
            .add("<TARGET>", getDisplayName(playerTarget))
            .build();

        plugin.message.sendMessage(player, "tp_requested", replacements2);

        // Wait 5 minutes to remove the teleport request
        (new BukkitRunnable() {
            public void run() {
                targetMap.remove(player.getUniqueId());
            }
        }).runTaskLaterAsynchronously((Plugin)this.plugin, 6000L);
        return true;
    }

    public boolean tpAcceptCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player player = (Player) sender;
        if (targetMap.containsValue(player.getUniqueId())) {
            for (Map.Entry<UUID, UUID> entry : targetMap.entrySet()) {
                if ((entry.getValue()).equals(player.getUniqueId())) {
                    Player requester = Bukkit.getPlayer(entry.getKey());
                    if (requester == null) {
                        targetMap.remove(entry.getKey());
                        plugin.message.sendMessage(player, "player_not_found");
                        return true;
                    }

                    Bill bill = plugin.invoice.teleportToPlayer(requester);

                    //send message with info to the player
                    int costNextTime = bill.next;
                    Replacements replacements = new Replacements.Builder()
                        .add("<CostNow>", String.valueOf(bill.total))
                        .add("<CostNextTime>", String.valueOf(costNextTime))
                        .build();

                    plugin.message.sendMessage(player, "tpaccept_accept");
                    plugin.message.sendMessage(requester, "tp_teleporting", replacements);

                    requester.teleport(player);
                    targetMap.remove(entry.getKey());
                    break;
                }
            }
        } else {
            plugin.message.sendMessage(player, "tpaccept_no_request");
        }
        return true;
    }

    public boolean tpDenyCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player player = (Player) sender;
        if (targetMap.containsValue(player.getUniqueId())) {
            for (Map.Entry<UUID, UUID> entry : targetMap.entrySet()) {
                if ((entry.getValue()).equals(player.getUniqueId())) {
                    targetMap.remove(entry.getKey());
                    Player playerSender = Bukkit.getPlayer(entry.getKey());

                    Replacements replacements = new Replacements.Builder()
                        .add("<TARGET>", getDisplayName(player))
                        .build();

                    plugin.message.sendMessage(playerSender, "tpdeny_denied_player", replacements);

                    plugin.message.sendMessage(playerSender, "tpdeny_denied_player", replacements);
                    plugin.message.sendMessage(player, "tpdeny_denied_target");
                    break;
                }
            }
        } else {
            plugin.message.sendMessage(player, "tpaccept_no_request");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1) {
            return null;
        }

        return List.of();
    }
}
