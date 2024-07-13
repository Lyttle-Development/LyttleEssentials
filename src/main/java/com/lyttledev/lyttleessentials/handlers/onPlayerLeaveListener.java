package com.lyttledev.lyttleessentials.handlers;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class onPlayerLeaveListener implements Listener {

    public onPlayerLeaveListener(LyttleEssentials plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        event.quitMessage(null);
        String[][] replacements = {{"<PLAYER>", getDisplayName(player)}};
        Message.sendBroadcast("leave_message", replacements);
    }
}
