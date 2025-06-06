package com.lyttledev.lyttleessentials;

import com.lyttledev.lyttleessentials.commands.*;
import com.lyttledev.lyttleessentials.handlers.*;
import com.lyttledev.lyttleessentials.types.Configs;
import com.lyttledev.lyttleessentials.types.Invoice;

import com.lyttledev.lyttleutils.utils.communication.Console;
import com.lyttledev.lyttleutils.utils.communication.Message;
import com.lyttledev.lyttleutils.utils.storage.GlobalConfig;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;

public final class LyttleEssentials extends JavaPlugin {
    public Economy economyImplementer;
    public Configs config;
    public Invoice invoice = new Invoice(this);
    public Console console;
    public Message message;
    public GlobalConfig global;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault or an economy plugin is not installed!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        // Setup config after creating the configs
        this.config = new Configs(this);
        this.global = new GlobalConfig(this);

        // Migrate config
        migrateConfig();

        // Plugin startup logic
        this.console = new Console(this);
        this.message = new Message(this, config.messages, global);

        // Commands
        new AdminTeleportCommand(this);
        new HomeCommand(this);
        new LyttleEssentialsCommand(this);
        new SpawnCommand(this);
        new TeleportCommand(this);
        new WarpCommand(this);
        new FlyCommand(this);
        new HealCommand(this);
        new TopCommand(this);
        new RepairCommand(this);
        new GamemodeCommand(this);

        // Listeners
        new onPlayerChatListener(this);
        new onPlayerJoinListener(this);
        new onPlayerLeaveListener(this);
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
        String configPath = "config.yml";
        if (!new File(getDataFolder(), configPath).exists())
            saveResource(configPath, false);

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
        String defaultGeneralPath =  defaultPath + configPath;
        saveResource(defaultGeneralPath, true);

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
            case "2":
                // Migrate config entries.
                config.messages.set("fly_usage", config.defaultMessages.get("fly_usage"));
                config.messages.set("fly_activate", config.defaultMessages.get("fly_activate"));
                config.messages.set("fly_deactivate", config.defaultMessages.get("fly_deactivate"));
                config.messages.set("fly_activate_other_sender", config.defaultMessages.get("fly_activate_other_sender"));
                config.messages.set("fly_deactivate_other_sender", config.defaultMessages.get("fly_deactivate_other_sender"));
                config.messages.set("fly_activate_other_target", config.defaultMessages.get("fly_activate_other_target"));
                config.messages.set("fly_deactivate_other_target", config.defaultMessages.get("fly_deactivate_other_target"));
                config.messages.set("fly_activate_console", config.defaultMessages.get("fly_activate_console"));
                config.messages.set("fly_deactivate_console", config.defaultMessages.get("fly_deactivate_console"));
                config.messages.set("chat_default_role", config.defaultMessages.get("chat_default_role"));
                config.messages.set("heal_usage", config.defaultMessages.get("heal_usage"));
                config.messages.set("heal_self", config.defaultMessages.get("heal_self"));
                config.messages.set("heal_other_sender", config.defaultMessages.get("heal_other_sender"));
                config.messages.set("heal_other_player", config.defaultMessages.get("heal_other_player"));
                config.messages.set("heal_console", config.defaultMessages.get("heal_console"));
                config.messages.set("first_join_message", config.defaultMessages.get("first_join_message"));
                config.messages.set("join_message", config.defaultMessages.get("join_message"));
                config.messages.set("leave_message", config.defaultMessages.get("leave_message"));
                config.messages.set("top_usage", config.defaultMessages.get("top_usage"));
                config.messages.set("top_self", config.defaultMessages.get("top_self"));
                config.messages.set("top_other_sender", config.defaultMessages.get("top_other_sender"));
                config.messages.set("top_other_player", config.defaultMessages.get("top_other_player"));
                config.messages.set("top_console", config.defaultMessages.get("top_console"));
                config.messages.set("repair_usage", config.defaultMessages.get("repair_usage"));
                config.messages.set("repair_helditem_self", config.defaultMessages.get("repair_helditem_self"));
                config.messages.set("repair_all_self", config.defaultMessages.get("repair_all_self"));
                config.messages.set("repair_helditem_other_sender", config.defaultMessages.get("repair_helditem_other_sender"));
                config.messages.set("repair_helditem_other_player", config.defaultMessages.get("repair_helditem_other_player"));
                config.messages.set("repair_helditem_other_console", config.defaultMessages.get("repair_helditem_other_console"));
                config.messages.set("repair_all_other_sender", config.defaultMessages.get("repair_all_other_sender"));
                config.messages.set("repair_all_other_player", config.defaultMessages.get("repair_all_other_player"));
                config.messages.set("repair_all_other_console", config.defaultMessages.get("repair_all_other_console"));
                config.messages.set("gamemode_usage", config.defaultMessages.get("gamemode_usage"));
                config.messages.set("gamemode_self", config.defaultMessages.get("gamemode_self"));
                config.messages.set("gamemode_other_sender", config.defaultMessages.get("gamemode_other_sender"));
                config.messages.set("gamemode_other_target", config.defaultMessages.get("gamemode_other_target"));
                config.messages.set("gamemode_console", config.defaultMessages.get("gamemode_console"));
                config.messages.set("gmx_usage", config.defaultMessages.get("gmx_usage"));

                // Update config version.
                config.general.set("config_version", 3);

                // Recheck if the config is fully migrated.
                migrateConfig();
                break;
            default:
                break;
        }
    }
}
