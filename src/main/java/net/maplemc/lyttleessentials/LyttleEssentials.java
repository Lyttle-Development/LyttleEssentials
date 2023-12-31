package net.maplemc.lyttleessentials;

import net.maplemc.lyttleessentials.commands.*;
import net.maplemc.lyttleessentials.types.Configs;
import net.maplemc.lyttleessentials.types.Invoice;
import net.maplemc.lyttleessentials.utils.*;

import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;

public final class LyttleEssentials extends JavaPlugin {
    public Economy economyImplementer;
    public Configs config = new Configs(this);
    public Invoice invoice = new Invoice(this);

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault or an economy plugin is not installed!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        // Plugin startup logic
        Console.init(this);
        Message.init(this);

        // Commands
        new AdminTeleportCommand(this);
        new HomeCommand(this);
        new LyttleEssentialsCommand(this);
        new SpawnCommand(this);
        new TeleportCommand(this);
        new WarpCommand(this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economyImplementer = rsp.getProvider();
        return economyImplementer != null;
    }

    @Override
    public void saveDefaultConfig() {
        if (!new File(getDataFolder(), "config.yml").exists())
            saveResource("config.yml", false);

        String messagesPath = this.getConfig().getString("configs.messages");
        if (messagesPath != null && !new File(getDataFolder(), messagesPath).exists())
            saveResource(messagesPath, false);

        String locationsPath = this.getConfig().getString("configs.locations");
        if (locationsPath != null && !new File(getDataFolder(), locationsPath).exists())
            saveResource(locationsPath, false);

        String homesPath = this.getConfig().getString("configs.homes");
        if (homesPath != null && !new File(getDataFolder(), homesPath).exists())
            saveResource(homesPath, false);

        String warpsPath = this.getConfig().getString("configs.warps");
        if (warpsPath != null && !new File(getDataFolder(), warpsPath).exists())
            saveResource(warpsPath, false);

        String shopsPath = this.getConfig().getString("configs.shops");
        if (shopsPath != null && !new File(getDataFolder(), shopsPath).exists())
            saveResource(shopsPath, false);
    }
}
