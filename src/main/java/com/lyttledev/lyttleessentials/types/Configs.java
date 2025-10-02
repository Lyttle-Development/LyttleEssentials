package com.lyttledev.lyttleessentials.types;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleutils.types.YamlConfig;

public class Configs {
    private final LyttleEssentials plugin;

    // Configs
    public YamlConfig general;
    public YamlConfig messages;
    public YamlConfig locations;
    public YamlConfig homes;
    public YamlConfig warps;
    public YamlConfig invoices;

    // Default configs
    public YamlConfig defaultMessages;
    public YamlConfig defaultLocations;
    public YamlConfig defaultHomes;
    public YamlConfig defaultWarps;
    public YamlConfig defaultInvoices;


    public Configs(LyttleEssentials plugin) {
        this.plugin = plugin;

        // Configs
        general = new YamlConfig(plugin, "config.yml");
        messages = new YamlConfig(plugin, "messages.yml");
        locations = new YamlConfig(plugin, "data/locations.yml");
        homes = new YamlConfig(plugin, "data/homes.yml");
        warps = new YamlConfig(plugin, "data/warps.yml");
        invoices = new YamlConfig(plugin, "data/invoices.yml");

        // Default configs
        defaultMessages = new YamlConfig(plugin, "#defaults/messages.yml");
        defaultLocations = new YamlConfig(plugin, "#defaults/data/locations.yml");
        defaultHomes = new YamlConfig(plugin, "#defaults/data/homes.yml");
        defaultWarps = new YamlConfig(plugin, "#defaults/data/warps.yml");
        defaultInvoices = new YamlConfig(plugin, "#defaults/data/invoices.yml");
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
