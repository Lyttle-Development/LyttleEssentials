package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.SelectorUtil.SelectorUtil;
import com.lyttledev.lyttleessentials.types.Bill;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.*;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class TeleportCommand implements CommandExecutor, TabCompleter {
    private final LyttleEssentials plugin;

    // requesterUUID -> (targetUUID -> payerUUID)
    static Map<UUID, Map<UUID, UUID>> requestMap = new HashMap<>();

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
        Player executor = (Player) sender;

        if (args.length < 1) {
            plugin.message.sendMessage(executor, "tp_usage");
            return true;
        }

        // /tp cancel  or  /tp cancel <selector> (cancel outgoing requests)
        if (args[0].equalsIgnoreCase("cancel")) {
            // cancel own outgoing requests
            if (args.length == 1) {
                requestMap.remove(executor.getUniqueId());
                plugin.message.sendMessage(executor, "tp_cancel_self");
                return true;
            }

            // cancel others' outgoing requests (requires .tp.other)
            if (!executor.hasPermission("lyttleessentials.tp.other")) {
                plugin.message.sendMessage(executor, "no_permission");
                return true;
            }

            List<Entity> requesters = SelectorUtil.resolveSelector(executor, args[1], true);
            if (requesters.isEmpty()) {
                plugin.message.sendMessage(executor, "player_not_found");
                return true;
            }

            for (Entity e : requesters) {
                if (!(e instanceof Player)) continue;
                Player req = (Player) e;

                requestMap.remove(req.getUniqueId());

                Replacements s = new Replacements.Builder()
                        .add("<TARGET>", getDisplayName(req))
                        .build();
                Replacements r = new Replacements.Builder()
                        .add("<PLAYER>", getDisplayName(executor))
                        .build();

                plugin.message.sendMessage(executor, "tp_cancel_other_sender", s);
                plugin.message.sendMessage(req, "tp_cancel_other_target", r);
            }
            return true;
        }

        // /tp <selector>  (requests from executor to each selected player)
        if (args.length == 1) {
            List<Entity> targets = SelectorUtil.resolveSelector(executor, args[0], true);
            if (targets.isEmpty()) {
                plugin.message.sendMessage(executor, "player_not_found");
                return true;
            }

            // send a message if the player does not have enough tokens (check only)
            Bill bill = plugin.invoice.teleportToPlayerCheck(executor);
            if (bill.total < 0) {
                Replacements replacements = new Replacements.Builder()
                        .add("<TOKENS>", String.valueOf(bill.total))
                        .build();

                plugin.message.sendMessage(executor, "tokens_missing_amount", replacements);
                return true;
            }

            for (Entity e : targets) {
                if (!(e instanceof Player)) continue;
                Player playerTarget = (Player) e;

                if (executor == playerTarget) {
                    plugin.message.sendMessage(executor, "tp_self");
                    continue;
                }

                // Initialize map for requester
                requestMap.computeIfAbsent(executor.getUniqueId(), k -> new HashMap<>());
                // Skip duplicate requests to same target
                if (requestMap.get(executor.getUniqueId()).containsKey(playerTarget.getUniqueId())) {
                    plugin.message.sendMessage(executor, "tp_already_requested");
                    continue;
                }

                Replacements replacements = new Replacements.Builder()
                        .add("<PLAYER>", getDisplayName(executor))
                        .build();

                plugin.message.sendMessage(playerTarget, "tp_ask_target", replacements);

                // Payer is the executor (same as requester) in 1-arg flow
                requestMap.get(executor.getUniqueId()).put(playerTarget.getUniqueId(), executor.getUniqueId());

                Replacements replacements2 = new Replacements.Builder()
                        .add("<TARGET>", getDisplayName(playerTarget))
                        .build();

                plugin.message.sendMessage(executor, "tp_requested", replacements2);
            }

            return true;
        }

        // /tp <selectorA> <selectorB>
        if (args.length == 2) {
            if (!executor.hasPermission("lyttleessentials.tp.other")) {
                plugin.message.sendMessage(executor, "no_permission");
                return true;
            }

            List<Entity> sources = SelectorUtil.resolveSelector(executor, args[0], true);
            List<Entity> targets = SelectorUtil.resolveSelector(executor, args[1], true);

            if (sources.isEmpty() || targets.isEmpty()) {
                plugin.message.sendMessage(executor, "player_not_found");
                return true;
            }

            // reject many -> many
            if (sources.size() > 1 && targets.size() > 1) {
                plugin.message.sendMessage(executor, "tp_many_to_many_not_allowed");
                return true;
            }

            // Normalize to players
            List<Player> srcPlayers = sources.stream().filter(p -> p instanceof Player).map(p -> (Player)p).toList();
            List<Player> tgtPlayers = targets.stream().filter(p -> p instanceof Player).map(p -> (Player)p).toList();
            if (srcPlayers.isEmpty() || tgtPlayers.isEmpty()) {
                plugin.message.sendMessage(executor, "player_not_found");
                return true;
            }

            // Check the executor can afford at least one (informative check)
            Bill billCheck = plugin.invoice.teleportToPlayerCheck(executor);
            if (billCheck.total < 0) {
                Replacements r = new Replacements.Builder()
                        .add("<TOKENS>", String.valueOf(billCheck.total))
                        .build();
                plugin.message.sendMessage(executor, "tokens_missing_amount", r);
                return true;
            }

            if (tgtPlayers.size() == 1) {
                Player target = tgtPlayers.get(0);
                for (Player src : srcPlayers) {
                    if (src == target) {
                        plugin.message.sendMessage(executor, "tp_self");
                        continue;
                    }

                    requestMap.computeIfAbsent(src.getUniqueId(), k -> new HashMap<>());
                    if (requestMap.get(src.getUniqueId()).containsKey(target.getUniqueId())) {
                        plugin.message.sendMessage(executor, "tp_already_requested");
                        continue;
                    }

                    Replacements toTarget = new Replacements.Builder()
                            .add("<PLAYER>", getDisplayName(src))
                            .build();
                    plugin.message.sendMessage(target, "tp_ask_target", toTarget);

                    // payer is executor
                    requestMap.get(src.getUniqueId()).put(target.getUniqueId(), executor.getUniqueId());

                    Replacements toSrc = new Replacements.Builder()
                            .add("<TARGET>", getDisplayName(target))
                            .build();
                    plugin.message.sendMessage(src, "tp_requested", toSrc);
                }
            } else {
                // sources.size()==1 and multiple targets
                Player src = srcPlayers.get(0);
                for (Player target : tgtPlayers) {
                    if (src == target) {
                        plugin.message.sendMessage(executor, "tp_self");
                        continue;
                    }

                    requestMap.computeIfAbsent(src.getUniqueId(), k -> new HashMap<>());
                    if (requestMap.get(src.getUniqueId()).containsKey(target.getUniqueId())) {
                        plugin.message.sendMessage(executor, "tp_already_requested");
                        continue;
                    }

                    Replacements toTarget = new Replacements.Builder()
                            .add("<PLAYER>", getDisplayName(src))
                            .build();
                    plugin.message.sendMessage(target, "tp_ask_target", toTarget);

                    // payer is executor
                    requestMap.get(src.getUniqueId()).put(target.getUniqueId(), executor.getUniqueId());

                    Replacements toSrc = new Replacements.Builder()
                            .add("<TARGET>", getDisplayName(target))
                            .build();
                    plugin.message.sendMessage(src, "tp_requested", toSrc);
                }
            }

            return true;
        }

        plugin.message.sendMessage(executor, "tp_usage");
        return true;
    }

    public boolean tpAcceptCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player player = (Player) sender; // player is the target who accepts
        boolean handled = false;

        for (Map.Entry<UUID, Map<UUID, UUID>> entry : new ArrayList<>(requestMap.entrySet())) {
            UUID requesterId = entry.getKey();
            Map<UUID, UUID> requestedTargets = entry.getValue();

            if (requestedTargets.containsKey(player.getUniqueId())) {
                Player requester = Bukkit.getPlayer(requesterId);
                if (requester == null) {
                    requestedTargets.remove(player.getUniqueId());
                    if (requestedTargets.isEmpty()) {
                        requestMap.remove(requesterId);
                    }
                    plugin.message.sendMessage(player, "player_not_found");
                    return true;
                }

                UUID payerId = requestedTargets.get(player.getUniqueId());
                Player payer = Bukkit.getPlayer(payerId);

                if (payer == null) {
                    // If payer is offline, treat as failure for now
                    requestedTargets.remove(player.getUniqueId());
                    if (requestedTargets.isEmpty()) {
                        requestMap.remove(requesterId);
                    }
                    plugin.message.sendMessage(player, "player_not_found");
                    return true;
                }

                Bill bill = plugin.invoice.teleportToPlayer(payer);

                int costNextTime = bill.next;
                Replacements replacements = new Replacements.Builder()
                        .add("<CostNow>", String.valueOf(bill.total))
                        .add("<CostNextTime>", String.valueOf(costNextTime))
                        .build();

                plugin.message.sendMessage(player, "tpaccept_accept");

                if (payer.getUniqueId().equals(requester.getUniqueId())) {
                    // requester pays (1-arg flow)
                    plugin.message.sendMessage(requester, "tp_teleporting", replacements);
                } else {
                    // executor (payer) pays on behalf of requester
                    plugin.message.sendMessage(payer, "tp_teleporting", replacements);

                    Replacements reqNotice = new Replacements.Builder()
                            .add("<TARGET>", getDisplayName(player))
                            .build();
                    plugin.message.sendMessage(requester, "tp_teleporting_requester", reqNotice);
                }

                requester.teleport(player);
                requestedTargets.remove(player.getUniqueId());
                if (requestedTargets.isEmpty()) {
                    requestMap.remove(requesterId);
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
        final Player player = (Player) sender; // player is the target who denies
        boolean handled = false;

        for (Map.Entry<UUID, Map<UUID, UUID>> entry : new ArrayList<>(requestMap.entrySet())) {
            UUID requesterId = entry.getKey();
            Map<UUID, UUID> requestedTargets = entry.getValue();

            if (requestedTargets.containsKey(player.getUniqueId())) {
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
                    requestMap.remove(requesterId);
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
        if (name.equals("tp")) {
            if (arguments.length == 1) {
                String cur = arguments[0] == null ? "" : arguments[0].toLowerCase();
                List<String> completions = new ArrayList<>();
                if ("cancel".startsWith(cur)) {
                    completions.add("cancel");
                }
                completions.addAll(SelectorUtil.selectorCompletions(arguments[0]));
                return completions;
            }
            if (arguments.length == 2) {
                if ("cancel".equalsIgnoreCase(arguments[0])) {
                    return SelectorUtil.selectorCompletions(arguments[1]);
                }
                return SelectorUtil.selectorCompletions(arguments[1], true);
            }
        }
        return List.of();
    }
}