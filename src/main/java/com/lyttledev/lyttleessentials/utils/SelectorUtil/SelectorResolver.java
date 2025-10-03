package com.lyttledev.lyttleessentials.utils.SelectorUtil;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Object-oriented selector resolver.
 * - Supports vanilla selectors via Bukkit.selectEntities(CommandSender, String)
 * - Adds fast-typing aliases:
 * "*"  -> all players
 * "**" -> all players except sender
 * "@s" -> self
 * "@p" -> nearest player
 * - Enforces per-selector permissions before invoking vanilla resolution:
 * - "lyttleessentials.selector.vanilla" is required to use vanilla selectors at all
 * - "@a" requires "lyttleessentials.selector.all"
 * - "*" requires "lyttleessentials.selector.all" (alias)
 * - "**" requires "lyttleessentials.selector.except_self" (alias)
 * - "@r" requires "lyttleessentials.selector.random"
 * - "@e" requires "lyttleessentials.selector.entities"
 * - "@p" and "@s" require "lyttleessentials.selector.vanilla" (no extra specific permission)
 * Notes:
 * - Must be called on the main server thread.
 * - Console can use selectors; selectors requiring a location may fail due to missing context.
 */
final class SelectorResolver {

    // Permissions
    public static final String PERM_VANILLA = "lyttleessentials.selector.vanilla";
    public static final String PERM_ALL = "lyttleessentials.selector.all";
    public static final String PERM_RANDOM = "lyttleessentials.selector.random";
    public static final String PERM_ENTITIES = "lyttleessentials.selector.entities";
    public static final String PERM_EXCEPT_SELF = "lyttleessentials.selector.except_self";

    SelectorResult resolve(CommandSender sender, String input, boolean playersOnly) {
        if (!Bukkit.isPrimaryThread()) {
            return SelectorResult.notOnMainThread();
        }
        if (input == null) {
            return SelectorResult.invalidInput("Selector input is null.");
        }
        String in = input.trim();
        if (in.isEmpty()) {
            return SelectorResult.invalidInput("Selector input is empty.");
        }

        // Fast aliases first
        if (in.equals("*") || in.equalsIgnoreCase("@a")) {
            if (!sender.hasPermission(PERM_ALL)) {
                return SelectorResult.noPermission("Missing permission: " + PERM_ALL);
            }
            if (playersOnly) {
                return SelectorResult.ok(new ArrayList<>(Bukkit.getOnlinePlayers()));
            } else {
                List<Entity> entities = Bukkit.getOnlinePlayers().stream()
                        .map(p -> (Entity) p)
                        .collect(Collectors.toList());
                return SelectorResult.ok(entities);
            }
        }

        if (in.equals("**")) {
            if (!(sender instanceof Player)) {
                return SelectorResult.invalidContext("The '**' alias requires a player as sender.");
            }
            if (!sender.hasPermission(PERM_EXCEPT_SELF)) {
                return SelectorResult.noPermission("Missing permission: " + PERM_EXCEPT_SELF);
            }
            Player self = (Player) sender;
            List<Player> others = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.equals(self))
                    .collect(Collectors.toList());
            return SelectorResult.ok(others);
        }

        if (in.equalsIgnoreCase("@r")) {
            if (!sender.hasPermission(PERM_RANDOM)) {
                return SelectorResult.noPermission("Missing permission: " + PERM_RANDOM);
            }
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (players.isEmpty()) {
                return SelectorResult.noMatches();
            }
            Player random = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            return SelectorResult.ok(Collections.singletonList(random));
        }

        if (in.equalsIgnoreCase("@s")) {
            if (!sender.hasPermission(PERM_VANILLA)) {
                return SelectorResult.noPermission("Missing permission: " + PERM_VANILLA);
            }
            if (!(sender instanceof Player)) {
                return SelectorResult.invalidContext("The '@s' selector requires a player as sender.");
            }
            Player self = (Player) sender;
            return SelectorResult.ok(Collections.singletonList(self));
        }

        if (in.equalsIgnoreCase("@p")) {
            if (!sender.hasPermission(PERM_VANILLA)) {
                return SelectorResult.noPermission("Missing permission: " + PERM_VANILLA);
            }
            if (!(sender instanceof Player)) {
                return SelectorResult.invalidContext("The '@p' selector requires a player as sender.");
            }
            Player src = (Player) sender;
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            if (players.isEmpty()) {
                return SelectorResult.noMatches();
            }
            // nearest includes self; restrict to same world
            Player nearest = players.stream()
                    .filter(p -> p.getWorld().equals(src.getWorld()))
                    .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(src.getLocation())))
                    .orElse(null);
            if (nearest == null) {
                return SelectorResult.noMatches();
            }
            return SelectorResult.ok(Collections.singletonList(nearest));
        }

        // Vanilla selectors (with potential arguments) using Bukkit API; enforce per-selector permissions
        if (in.startsWith("@")) {
            if (!sender.hasPermission(PERM_VANILLA)) {
                return SelectorResult.noPermission("Missing permission: " + PERM_VANILLA);
            }

            SelectorType kind = SelectorType.fromInput(in);
            switch (kind) {
                case ALL:
                    if (!sender.hasPermission(PERM_ALL)) {
                        return SelectorResult.noPermission("Missing permission: " + PERM_ALL);
                    }
                    break;
                case RANDOM:
                    if (!sender.hasPermission(PERM_RANDOM)) {
                        return SelectorResult.noPermission("Missing permission: " + PERM_RANDOM);
                    }
                    break;
                case ENTITIES:
                    if (!sender.hasPermission(PERM_ENTITIES)) {
                        return SelectorResult.noPermission("Missing permission: " + PERM_ENTITIES);
                    }
                    break;
                case NEAREST:
                case SELF:
                case UNKNOWN:
                default:
                    // No additional specific permission beyond vanilla for @p/@s/unknown
                    break;
            }

            try {
                List<Entity> selected = Bukkit.selectEntities(sender, in);
                if (playersOnly) {
                    selected = selected.stream().filter(e -> e instanceof Player).collect(Collectors.toList());
                }
                if (selected.isEmpty()) {
                    return SelectorResult.noMatches();
                }
                return SelectorResult.ok(selected);
            } catch (IllegalArgumentException iae) {
                return SelectorResult.invalidSelector("Invalid selector: " + in);
            } catch (Exception ex) {
                return SelectorResult.error("Error resolving selector: " + ex.getMessage());
            }
        }

        // Fallback: exact player name (case-sensitive)
        Player exact = Bukkit.getPlayerExact(in);
        if (exact != null) {
            return SelectorResult.ok(Collections.singletonList(exact));
        }

        // Entity UUID (only if UUID-like)
        try {
            UUID uuid = UUID.fromString(in);
            for (World w : Bukkit.getWorlds()) {
                Entity e = w.getEntity(uuid);
                if (e != null) {
                    if (playersOnly && !(e instanceof Player)) {
                        return SelectorResult.noMatches();
                    }
                    return SelectorResult.ok(Collections.singletonList(e));
                }
            }
            return SelectorResult.noMatches();
        } catch (IllegalArgumentException ignored) {
            // not a UUID; fall through
        }

        return SelectorResult.noMatches();
    }
}
