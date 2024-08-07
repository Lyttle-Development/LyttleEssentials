package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.types.Bill;
import com.lyttledev.lyttleessentials.utils.Console;
import com.lyttledev.lyttleessentials.utils.Message;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SpawnCommand implements CommandExecutor, TabCompleter {
    private LyttleEssentials plugin;

    public SpawnCommand(LyttleEssentials plugin) {
        plugin.getCommand("spawn").setExecutor(this);
        plugin.getCommand("setspawn").setExecutor(this);
        plugin.getCommand("delspawn").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Message.sendConsole("must_be_player");
            return true;
        }

        Player player = (Player) sender;
        if (player.hasPermission("mc.admin")) {
            if (label.equalsIgnoreCase("setspawn")) {
                Location spawn = player.getLocation();
                plugin.config.locations.set("spawn", spawn);
                Console.playerCommand(player, "setworldspawn");
                Message.sendPlayer(player, "spawn_set");
                return true;
            }
            if (label.equalsIgnoreCase("delspawn")) {
                plugin.config.locations.set("spawn", null);
                Message.sendPlayer(player, "spawn_deleted");
                return true;
            }
        }

        if (plugin.config.locations.get("spawn") == null) {
            Message.sendPlayer(player, "spawn_not_set");
            return true;
        }

        Bill bill = plugin.invoice.teleportToSpawn(player);
        if (bill.total < 0) {
            Message.sendPlayer(player, "tokens_missing");
            return true;
        }

        Location spawn = (Location) plugin.config.locations.get("spawn");
        player.teleport(spawn);
        String[][] replacements = {{"<PRICE>", String.valueOf(bill.total)}};
        Message.sendPlayer(player, "spawn_teleported", replacements);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        return Arrays.asList();
    }
}
