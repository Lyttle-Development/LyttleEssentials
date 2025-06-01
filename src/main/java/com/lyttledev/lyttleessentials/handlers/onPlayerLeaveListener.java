package com.lyttledev.lyttleessentials.handlers;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class onPlayerLeaveListener implements Listener {
    LyttleEssentials plugin;

    public onPlayerLeaveListener(LyttleEssentials plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        event.quitMessage(null);
        Replacements replacements = new Replacements.Builder()
                .add("<PLAYER>", getDisplayName(player))
                .build();
        plugin.message.sendBroadcast("leave_message", replacements);
    }
}
