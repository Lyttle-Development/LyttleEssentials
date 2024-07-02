package com.lyttledev.lyttleessentials.handlers;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class onPlayerJoinListener implements Listener {
    private LyttleEssentials plugin;

    public onPlayerJoinListener(LyttleEssentials plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        String[][] replacements = {{"<PLAYER>", player.getDisplayName()}};
        event.joinMessage(null);

        if (!player.hasPlayedBefore()) {
            Message.sendBroadcast("first_join_message", replacements);
            return;
        }

        Message.sendBroadcast("join_message", replacements);
    }

}
