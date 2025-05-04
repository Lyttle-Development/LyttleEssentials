package com.lyttledev.lyttleessentials.handlers;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class onPlayerJoinListener implements Listener {
    private final LyttleEssentials plugin;

    public onPlayerJoinListener(LyttleEssentials plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        String[][] replacements = {{"<PLAYER>", getDisplayName(player)}};
        event.joinMessage(null);

        if (!player.hasPlayedBefore()) {
            plugin.message.sendBroadcast("first_join_message", replacements);
            return;
        }

        plugin.message.sendBroadcast("join_message", replacements);
    }

}
