package com.lyttledev.lyttleessentials;

import com.lyttledev.lyttleessentials.commands.*;
import com.lyttledev.lyttleessentials.handlers.*;
import com.lyttledev.lyttleessentials.types.Configs;
import com.lyttledev.lyttleessentials.types.Invoice;
import com.lyttledev.lyttleessentials.utils.*;

import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;

public final class LyttleEssentials extends JavaPlugin {
    public Economy economyImplementer;
    public Configs config;
    public Invoice invoice = new Invoice(this);

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault or an economy plugin is not installed!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        // Setup config after creating the configs
        config = new Configs(this);
        // Migrate config
        migrateConfig();

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

        // Listeners
        new onPlayerChatListener(this);
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

        String messagesPath = "messages.yml";
        if (!new File(getDataFolder(), messagesPath).exists())
            saveResource(messagesPath, false);

        String locationsPath = "data/locations.yml";
        if (!new File(getDataFolder(), locationsPath).exists())
            saveResource(locationsPath, false);

        String homesPath = "data/homes.yml";
        if (!new File(getDataFolder(), homesPath).exists())
            saveResource(homesPath, false);

        String warpsPath = "data/warps.yml";
        if (!new File(getDataFolder(), warpsPath).exists())
            saveResource(warpsPath, false);

        String invoicesPath = "data/invoices.yml";
        if (!new File(getDataFolder(), invoicesPath).exists())
            saveResource(invoicesPath, false);

        String chatFilterPath = "data/chat_filter.txt";
        if (!new File(getDataFolder(), chatFilterPath).exists())
            saveResource(chatFilterPath, false);

        // Defaults:
        String defaultPath = "#defaults/";
        String defaultMessagesPath =  defaultPath + messagesPath;
        saveResource(defaultMessagesPath, true);

        String defaultLocationsPath =  defaultPath + locationsPath;
        saveResource(defaultLocationsPath, true);

        String defaultHomesPath =  defaultPath + homesPath;
        saveResource(defaultHomesPath, true);

        String defaultWarpsPath =  defaultPath + warpsPath;
        saveResource(defaultWarpsPath, true);

        String defaultInvoicesPath =  defaultPath + invoicesPath;
        saveResource(defaultInvoicesPath, true);
    }

    private void migrateConfig() {
        if (!config.general.contains("config_version")) {
            config.general.set("config_version", 0);
        }

        switch (config.general.get("config_version").toString()) {
            case "0":
                // Migrate config entries.
                config.general.remove("configs.messages");
                config.general.remove("configs.locations");
                config.general.remove("configs.homes");
                config.general.remove("configs.warps");
                config.general.remove("configs.shops");
                config.general.remove("configs.invoices");
                config.general.remove("configs");

                // Update config version.
                config.general.set("config_version", 1);

                // Recheck if the config is fully migrated.
                migrateConfig();
                break;
            case "1":
                // Migrate config entries.
                config.messages.set("chat_format", config.defaultMessages.get("chat_format"));

                // Update config version.
                config.general.set("config_version", 2);

                // Recheck if the config is fully migrated.
                migrateConfig();
                break;
            default:
                break;
        }


    }
}
