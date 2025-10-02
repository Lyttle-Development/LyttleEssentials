package com.lyttledev.lyttleessentials.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemoryClass {

    private static List<UUID> vanishedPlayers = new ArrayList<>();

    public static void hidePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        vanishedPlayers.add(uuid);
    }

    public static void showPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        vanishedPlayers.remove(uuid);
    }

    public static boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public static void showList() {
        Bukkit.broadcastMessage("VANISH LIST:");
        for (UUID uuid : vanishedPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            Bukkit.broadcastMessage(player.getName());
        }
        Bukkit.broadcastMessage("----");
    }
}
