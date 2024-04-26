package com.lyttledev.lyttleessentials.types;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class Warp {
    public String owner;
    public String name;
    public Location location;

    public Warp(Player player, String name) {
        this.owner = player.getUniqueId().toString();
        this.name = name;
        this.location = player.getLocation();
    }

    public Warp(ConfigurationSection warpSection) {
        this.owner = warpSection.getString("owner");
        this.name = warpSection.getString("name");
        this.location = warpSection.getLocation("location");
    }
}

