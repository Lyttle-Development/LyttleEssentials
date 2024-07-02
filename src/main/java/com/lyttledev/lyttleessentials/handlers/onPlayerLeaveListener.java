package com.lyttledev.lyttleessentials.handlers;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class onPlayerLeaveListener implements Listener {
    private LyttleEssentials plugin;

    public onPlayerLeaveListener(LyttleEssentials plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        event.quitMessage(null);
        String[][] replacements = {{"<PLAYER>", player.getDisplayName()}};
        Message.sendBroadcast("leave_message", replacements);
    }
}
