package net.maplemc.lyttleessentials.types;

import net.maplemc.lyttleessentials.LyttleEssentials;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

public class Configs {
    private final LyttleEssentials plugin;

    public Config general;
    public Config messages;
    public Config locations;
    public Config homes;
    public Config warps;
    public Config invoices;

    public Configs(LyttleEssentials plugin) {
        this.plugin = plugin;

        general = new Config(plugin, "config.yml");
        messages = new Config(plugin, getConfigPath("messages"));
        locations = new Config(plugin, getConfigPath("locations"));
        homes = new Config(plugin, getConfigPath("homes"));
        warps = new Config(plugin, getConfigPath("warps"));
        invoices = new Config(plugin, getConfigPath("invoices"));
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
