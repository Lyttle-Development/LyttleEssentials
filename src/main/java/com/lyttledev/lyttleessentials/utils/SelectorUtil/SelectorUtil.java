package com.lyttledev.lyttleessentials.utils.SelectorUtil;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility for parsing selectors and aliases in commands.
 * Uses vanilla /execute to resolve selectors for full compatibility with Minecraft's selector logic.
 */
public class SelectorUtil {

    private static final SelectorResolver RESOLVER = new SelectorResolver();

    /**
     * Resolves a selector or player/entity name to a list of players/entities, using vanilla selector logic.
     * Custom aliases:
     *   - "*" for all players
     *   - "**" for all players except sender
     * Permissions:
     *   - "lyttleessentials.selector.all" for @a or *
     *   - "lyttleessentials.selector.random" for @r
     *   - "lyttleessentials.selector.entities" for @e or @e[type=TYPE]
     *   - "lyttleessentials.selector.except_self" for **
     *   - "lyttleessentials.selector.vanilla" for vanilla selector usage
     */
    public static List<Entity> resolveSelector(CommandSender sender, String input, boolean playersOnly) {
        // Delegate to the object-oriented resolver; preserve legacy behavior of returning only a list.
        SelectorResult result = RESOLVER.resolve(sender, input, playersOnly);
        return result.getEntities();
    }

    /**
     * Object-oriented variant that returns a SelectorResult with status and message.
     * Prefer this in new code for better error handling and permission feedback.
     *
     * @param sender the command sender
     * @param input the selector or alias
     * @param playersOnly if true, return only Player entities
     * @return a detailed SelectorResult
     */
    public static SelectorResult resolveSelectorDetailed(CommandSender sender, String input, boolean playersOnly) {
        return RESOLVER.resolve(sender, input, playersOnly);
    }

    /**
     * Uses vanilla /execute command to resolve selectors using Minecraft's selector logic.
     * @param executor The player running the command (context for location, world, etc)
     * @param selector The selector string (e.g. @a[distance=..10])
     * @param playersOnly If true, return only Player entities
     * @return List of matching entities
     */
    @Deprecated
    @SuppressWarnings("unused")
    private static List<Entity> resolveVanillaSelector(Player executor, String selector, boolean playersOnly) {
        // Kept for backward compatibility with older references; prefer SelectorResolver.
        // Run: /execute as <executor> at <executor> run data get entity <selector> UUID
        // Instead, we use Bukkit API for vanilla selectors
        // Bukkit API supports selectEntities since 1.13+
        List<Entity> result = Bukkit.selectEntities(executor, selector);
        if (playersOnly) {
            return result.stream()
                    .filter(e -> e instanceof Player)
                    .collect(Collectors.toList());
        }
        return result;
    }

    public static List<String> selectorCompletions(String current) {
        List<String> base = new ArrayList<>(Arrays.asList("@a", "@p", "@r", "@s", "*", "**", "@e"));
        for (Player p : Bukkit.getOnlinePlayers()) base.add(p.getName());
        String pref = current == null ? "" : current.toLowerCase();
        List<String> out = new ArrayList<>();
        for (String opt : base) if (opt.toLowerCase().startsWith(pref)) out.add(opt);
        return out;
    }

    /**
     * Selector completions with filtering for second-argument scenarios.
     * When singleTargetOnly is true, we hide common multi-target selectors like "@a", "*", "**".
     * Note: We keep "@p", "@s", "@r" (random selects a single), and explicit player names.
     */
    public static List<String> selectorCompletions(String current, boolean singleTargetOnly) {
        if (!singleTargetOnly) return selectorCompletions(current);
        Set<String> disallow = new HashSet<>(Arrays.asList("@a", "*", "**", "@e"));
        return selectorCompletions(current, disallow);
    }

    /**
     * Selector completions with a disallow list for fine-grained control per command/argument.
     */
    public static List<String> selectorCompletions(String current, Set<String> disallow) {
        List<String> base = new ArrayList<>(Arrays.asList("@a", "@p", "@r", "@s", "*", "**", "@e"));
        for (Player p : Bukkit.getOnlinePlayers()) base.add(p.getName());
        String pref = current == null ? "" : current.toLowerCase();
        List<String> out = new ArrayList<>();
        for (String opt : base) {
            if (disallow != null && disallow.contains(opt)) continue;
            if (opt.toLowerCase().startsWith(pref)) out.add(opt);
        }
        return out;
    }
}