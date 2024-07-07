package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.Console;
import com.lyttledev.lyttleessentials.utils.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class AdminTeleportCommand implements Command<CommandSourceStack> {
    private static LyttleEssentials plugin;


    public static void register(LyttleEssentials pl, Commands commands) {
        // Get the economy from Vault
        plugin = pl;

        // atp command
        LiteralArgumentBuilder<CommandSourceStack> commandBuilder =
            Commands.literal("atp")
                    // atp <target-player>
                    .then(Commands.argument("target", ArgumentTypes.player())
                            .executes(new AdminTeleportCommand()))
                    .then(Commands.argument("target2", ArgumentTypes.player())
                            .executes(new AdminTeleportCommand()));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Entity entity = source.getExecutor();


        if (!(entity instanceof Player)) {
            Message.sendMessage(entity,"must_be_player");
            return 0;
        }

        Player player = (Player) entity;

        if (!player.hasPermission("lyttleessentials.admintp")) {
            Message.sendMessage(player, "no_permission");
            return 0;
        }

        if (args.length == 1) {
            if (!player.hasPermission("lyttleessentials.admintp.self")) {
                Message.sendMessage(player, "no_permission");
                return 0;
            }


            Player target = plugin.getServer().getPlayer(args[0]);

            if (target == null) {
                Message.sendMessage(player, "player_not_found");
                return true;
            }

            player.teleport(target);

            String[][] messageReplacements = {
                { "<USER>", getDisplayName(player) },
                { "<TARGET>", getDisplayName(target) }
            };
            Message.sendMessage(player, "atp_user", messageReplacements);
            return true;
        }

        if (!player.hasPermission("lyttleessentials.admintp.other")) {
            Message.sendMessage(sender, "no_permission");
            return true;
        }

        if (args.length == 2) {
            Player user = plugin.getServer().getPlayer(args[0]);
            Player target = plugin.getServer().getPlayer(args[1]);

            if (user == null || target == null) {
                Message.sendMessage(player, "player_not_found");
                return true;
            }

            user.teleport(target);

            String[][] messageReplacements = {
                { "<USER>", getDisplayName(user) },
                { "<TARGET>", getDisplayName(target) }
            };
            Message.sendMessage(player, "atp_user", messageReplacements);
            return true;
        }

        if (args.length == 3) {
            Console.playerCommand(player, "minecraft:tp " + args[0] + " " + args[1] + " " + args[2]);

            String[][] messageReplacements = {
                    { "<USER>", getDisplayName(player) },
                    { "<TARGET>", "Loc(" + args[0] + ", " + args[1] + ", " + args[2] + ")" }
            };
            return true;
        }

        if (args.length == 4) {
            Player user = plugin.getServer().getPlayer(args[0]);

            if (user == null) {
                Message.sendMessage(player, "player_not_found");
                return true;
            }

            Console.command("minecraft:execute as " +  getDisplayName(user) + " at @s run tp " + args[1] + " " + args[2] + " " + args[3]);

            String[][] messageReplacements = {
                    { "<USER>", getDisplayName(user) },
                    { "<TARGET>", "Loc(" + args[1] + ", " + args[2] + ", " + args[3] + ")" }
            };
            Message.sendMessage(player, "atp_user", messageReplacements);
            return true;
        }

        Message.sendMessage(player, "atp_usage");

        return true;
    }

//    @Override
//    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
//        if (arguments.length <= 2) {
//            return null;
//        }
//
//        return List.of();
//    }
}
