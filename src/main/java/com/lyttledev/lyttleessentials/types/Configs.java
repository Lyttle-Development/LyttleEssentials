package com.lyttledev.lyttleessentials.types;

import com.lyttledev.lyttleessentials.LyttleEssentials;

public class Configs {
    private final LyttleEssentials plugin;

    // Configs
    public Config general;
    public Config messages;
    public Config locations;
    public Config homes;
    public Config warps;
    public Config invoices;

    // Default configs
    public Config defaultMessages;
    public Config defaultLocations;
    public Config defaultHomes;
    public Config defaultWarps;
    public Config defaultInvoices;


    public Configs(LyttleEssentials plugin) {
        this.plugin = plugin;

        // Configs
        general = new Config(plugin, "config.yml");
        messages = new Config(plugin, "messages.yml");
        locations = new Config(plugin, "data/locations.yml");
        homes = new Config(plugin, "data/homes.yml");
        warps = new Config(plugin, "data/warps.yml");
        invoices = new Config(plugin, "data/invoices.yml");

        // Default configs
        defaultMessages = new Config(plugin, "#defaults/messages.yml");
        defaultLocations = new Config(plugin, "#defaults/data/locations.yml");
        defaultHomes = new Config(plugin, "#defaults/data/homes.yml");
        defaultWarps = new Config(plugin, "#defaults/data/warps.yml");
        defaultInvoices = new Config(plugin, "#defaults/data/invoices.yml");
    }

    public void reload() {
        general.reload();
        messages.reload();
        homes.reload();
        warps.reload();
        invoices.reload();

        plugin.reloadConfig();
    }

    private String getConfigPath(String path) {
        return plugin.getConfig().getString("configs." + path);
    }
}
