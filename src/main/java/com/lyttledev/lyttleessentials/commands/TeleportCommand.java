package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.types.Bill;
import com.lyttledev.lyttleessentials.utils.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;

import java.util.*;

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
            Message.sendConsole("must_be_player");
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
            Message.sendPlayer(player, "player_not_found");
            return true;
        }

        // Stores some crucial info
        Player playerTarget = Bukkit.getPlayer(args[0]);

        // Checks for a TP to the player himself
        if (player == playerTarget) {
            Message.sendPlayer(player, "tp_self");
            return true;
        }

        // Check if the player already has a /tp request
        if (targetMap.containsKey(player.getUniqueId())) {
            Message.sendPlayer(player, "tp_already_requested");
            return true;
        }

        // send a message if the player does not have enough tokens
        Bill bill = plugin.invoice.teleportToPlayerCheck(player);
        if (bill.total < 0) {
            String[][] replacements = {
                    {"<TOKENS>", String.valueOf(bill.total)},
            };
            Message.sendPlayer(player, "tokens_missing_amount", replacements);
            return true;
        }

        String[][] replacements = {
                {"<PLAYER>", player.getDisplayName()}
        };
        Message.sendPlayer(playerTarget, "tp_ask_target", replacements);

        targetMap.put(player.getUniqueId(), playerTarget.getUniqueId());

        String[][] replacements2 = {
                {"<TARGET>", playerTarget.getDisplayName()}
        };
        Message.sendPlayer(player, "tp_requested", replacements2);

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
                        Message.sendPlayer(player, "player_not_found");
                        return true;
                    }

                    Bill bill = plugin.invoice.teleportToPlayer(requester);

                    //send message with info to the player
                    int costNextTime = bill.next;
                    String[][] replacements = {
                            {"<CostNow>", String.valueOf(bill.total)},
                            {"<CostNextTime>", String.valueOf(costNextTime)}
                    };
                    Message.sendPlayer(player, "tpaccept_accept");
                    Message.sendPlayer(requester, "tp_teleporting", replacements);

                    requester.teleport(player);
                    targetMap.remove(entry.getKey());
                    break;
                }
            }
        } else {
            Message.sendPlayer(player, "tpaccept_no_request");
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
                    String[][] replacements = {
                            {"<TARGET>", player.getDisplayName()}
                    };
                    Message.sendPlayer(playerSender, "tpdeny_denied_player", replacements);


                    String[][] replacements2 = {
                            {"<TARGET>", player.getDisplayName()}
                    };
                    Message.sendPlayer(playerSender, "tpdeny_denied_player", replacements2);
                    Message.sendPlayer(player, "tpdeny_denied_target");
                    break;
                }
            }
        } else {
            Message.sendPlayer(player, "tpaccept_no_request");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1) {
            return null;
        }

        return Arrays.asList();
    }
}
