package net.maplemc.lyttleessentials.commands;

import net.maplemc.lyttleessentials.LyttleEssentials;
import net.maplemc.lyttleessentials.utils.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LyttleEssentialsCommand implements CommandExecutor, TabCompleter {
    private LyttleEssentials plugin;

    public LyttleEssentialsCommand(LyttleEssentials plugin) {
        plugin.getCommand("lyttleessentials").setExecutor(this);
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // Check for permission
        if (!(sender.hasPermission("mc.lyttleessentials") || sender.hasPermission("mc.admin"))) {
            Message.sendPlayer((Player) sender, "no_permission", Message.noReplacements);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("plugin version: 2.0.0");
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.config.reload();
                Message.sendPlayerRaw((Player) sender, "The config has been reloaded");
            }
        }
        return true;
    }

    private List<String> arguments = new ArrayList<String>();
    private List<String> limit = new ArrayList<String>();
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        List<String> result = new ArrayList<String>();

        if (arguments.isEmpty()) {
            arguments.add("reload");
        }

        if (args.length == 1) {
            for (String x : arguments) {
                if (x.toLowerCase().startsWith(args[0]))
                    result.add(x);
            }
            return result;
        }

        if (args.length >= 2) {
            for (String x : limit) {
                if (x.toLowerCase().startsWith(args[0]))
                    result.add(x);
            }
            return result;
        }

        return null;
    }
}
