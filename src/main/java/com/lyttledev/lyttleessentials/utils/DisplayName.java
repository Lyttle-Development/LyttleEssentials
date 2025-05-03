package com.lyttledev.lyttleessentials.utils;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;

public class DisplayName {
    public static String getDisplayName(Player player) { return ((TextComponent) player.displayName()).content(); }
}
