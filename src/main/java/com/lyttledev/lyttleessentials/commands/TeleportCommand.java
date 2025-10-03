package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.SelectorUtil.SelectorUtil;
import com.lyttledev.lyttleessentials.types.Bill;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class TeleportCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    static Map<UUID, Set<UUID>> targetMap = new HashMap<>();

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

        List<Entity> targets = SelectorUtil.resolveSelector(player, args[0], true);
        if (targets.isEmpty()) {
            plugin.message.sendMessage(player, "player_not_found");
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

        for (Entity e : targets) {
            if (!(e instanceof Player)) continue;
            Player playerTarget = (Player) e;

            // Checks for a TP to the player himself
            if (player == playerTarget) {
                plugin.message.sendMessage(player, "tp_self");
                continue;
            }

            // Initialize requester set
            targetMap.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());

            // Skip duplicate requests to same target
            if (targetMap.get(player.getUniqueId()).contains(playerTarget.getUniqueId())) {
                plugin.message.sendMessage(player, "tp_already_requested");
                continue;
            }

            Replacements replacements = new Replacements.Builder()
                    .add("<PLAYER>", getDisplayName(player))
                    .build();

            plugin.message.sendMessage(playerTarget, "tp_ask_target", replacements);

            targetMap.get(player.getUniqueId()).add(playerTarget.getUniqueId());

            Replacements replacements2 = new Replacements.Builder()
                    .add("<TARGET>", getDisplayName(playerTarget))
                    .build();

            plugin.message.sendMessage(player, "tp_requested", replacements2);
        }

        // Wait 5 minutes to remove the teleport requests for this requester
        (new BukkitRunnable() {
            public void run() {
                targetMap.remove(player.getUniqueId());
            }
        }).runTaskLaterAsynchronously((Plugin) TeleportCommand.this.plugin, 6000L);
        return true;
    }

    public boolean tpAcceptCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player player = (Player) sender;
        boolean handled = false;

        for (Map.Entry<UUID, Set<UUID>> entry : new ArrayList<>(targetMap.entrySet())) {
            UUID requesterId = entry.getKey();
            Set<UUID> requestedTargets = entry.getValue();
            if (requestedTargets.contains(player.getUniqueId())) {
                Player requester = Bukkit.getPlayer(requesterId);
                if (requester == null) {
                    requestedTargets.remove(player.getUniqueId());
                    if (requestedTargets.isEmpty()) {
                        targetMap.remove(requesterId);
                    }
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
                requestedTargets.remove(player.getUniqueId());
                if (requestedTargets.isEmpty()) {
                    targetMap.remove(requesterId);
                }
                handled = true;
                break;
            }
        }

        if (!handled) {
            plugin.message.sendMessage(player, "tpaccept_no_request");
        }
        return true;
    }

    public boolean tpDenyCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player player = (Player) sender;
        boolean handled = false;

        for (Map.Entry<UUID, Set<UUID>> entry : new ArrayList<>(targetMap.entrySet())) {
            UUID requesterId = entry.getKey();
            Set<UUID> requestedTargets = entry.getValue();

            if (requestedTargets.contains(player.getUniqueId())) {
                requestedTargets.remove(player.getUniqueId());
                Player playerSender = Bukkit.getPlayer(requesterId);

                Replacements replacements = new Replacements.Builder()
                        .add("<TARGET>", getDisplayName(player))
                        .build();

                if (playerSender != null) {
                    plugin.message.sendMessage(playerSender, "tpdeny_denied_player", replacements);
                    plugin.message.sendMessage(playerSender, "tpdeny_denied_player", replacements);
                }
                plugin.message.sendMessage(player, "tpdeny_denied_target");

                if (requestedTargets.isEmpty()) {
                    targetMap.remove(requesterId);
                }
                handled = true;
                break;
            }
        }

        if (!handled) {
            plugin.message.sendMessage(player, "tpaccept_no_request");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        String name = command.getName().toLowerCase();
        if (name.equals("tp") && arguments.length == 1) {
            return SelectorUtil.selectorCompletions(arguments[0]);
        }
        return List.of();
    }
}