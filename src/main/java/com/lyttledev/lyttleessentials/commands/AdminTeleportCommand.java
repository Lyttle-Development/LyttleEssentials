package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.Console;
import com.lyttledev.lyttleessentials.utils.Message;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class AdminTeleportCommand implements BasicCommand, TabCompleter {
    private LyttleEssentials plugin;

    public AdminTeleportCommand(LyttleEssentials plugin) {
        this.plugin = plugin;
    }

    public static void register(LyttleEssentials plugin, LifecycleEventManager<Plugin> manager) {
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("atp", "TP", new AdminTeleportCommand(plugin));
        });
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player)) {
            Message.sendMessage(sender,"must_be_player");
            return;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lyttleessentials.admintp")) {
            Message.sendMessage((Player) sender, "no_permission");
            return;
        }

        if (args.length == 1) {
            if (!player.hasPermission("lyttleessentials.admintp.self")) {
                Message.sendMessage(sender, "no_permission");
                return;
            }

            Player target = plugin.getServer().getPlayer(args[0]);

            if (target == null) {
                Message.sendMessage(player, "player_not_found");
                return;
            }

            player.teleport(target);

            String[][] messageReplacements = {
                { "<USER>", getDisplayName(player) },
                { "<TARGET>", getDisplayName(target) }
            };
            Message.sendMessage(player, "atp_user", messageReplacements);
            return;
        }

        if (!player.hasPermission("lyttleessentials.admintp.other")) {
            Message.sendMessage(sender, "no_permission");
            return;
        }

        if (args.length == 2) {
            Player user = plugin.getServer().getPlayer(args[0]);
            Player target = plugin.getServer().getPlayer(args[1]);

            if (user == null || target == null) {
                Message.sendMessage(player, "player_not_found");
                return;
            }

            user.teleport(target);

            String[][] messageReplacements = {
                { "<USER>", getDisplayName(user) },
                { "<TARGET>", getDisplayName(target) }
            };
            Message.sendMessage(player, "atp_user", messageReplacements);
            return;
        }

        if (args.length == 3) {
            Console.playerCommand(player, "minecraft:tp " + args[0] + " " + args[1] + " " + args[2]);

            String[][] messageReplacements = {
                    { "<USER>", getDisplayName(player) },
                    { "<TARGET>", "Loc(" + args[0] + ", " + args[1] + ", " + args[2] + ")" }
            };
            return;
        }

        if (args.length == 4) {
            Player user = plugin.getServer().getPlayer(args[0]);

            if (user == null) {
                Message.sendMessage(player, "player_not_found");
                return;
            }

            Console.command("minecraft:execute as " +  getDisplayName(user) + " at @s run tp " + args[1] + " " + args[2] + " " + args[3]);

            String[][] messageReplacements = {
                    { "<USER>", getDisplayName(user) },
                    { "<TARGET>", "Loc(" + args[1] + ", " + args[2] + ", " + args[3] + ")" }
            };
            Message.sendMessage(player, "atp_user", messageReplacements);
            return;
        }

        Message.sendMessage(player, "atp_usage");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length <= 2) {
            return null;
        }

        return List.of();
    }
}
