package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleessentials.utils.Console;
import com.lyttledev.lyttleessentials.utils.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class AdminTeleportCommand {
    private static LyttleEssentials plugin;
    private static final String commandPermission = "lyttleessentials.admintp";


    public static void register(LyttleEssentials pl, Commands commands) {
        // Get the economy from Vault
        plugin = pl;

        // atp command
        LiteralArgumentBuilder<CommandSourceStack> commandBuilder =
            Commands.literal("atp")
                .requires(source -> source.getSender().hasPermission(commandPermission))

                // Teleport a player to another player or location
                .then(Commands.argument("source", ArgumentTypes.player())
                    .requires(source -> source.getSender().hasPermission(commandPermission + ".other"))
                    // Teleport a player to another player
                    .then(Commands.argument("target", ArgumentTypes.player())
                        .executes(source -> new AdminTeleportCommand().teleportSourceToTarget(source)))

                    // Teleport a player to a location
                    .then(Commands.argument("world", ArgumentTypes.world())
                        .then(Commands.argument("location", ArgumentTypes.blockPosition())
                            .executes(source -> new AdminTeleportCommand().teleportSourceToLocation(source))
                        )
                    )
                )

                // Teleport to a location
                .then(Commands.argument("world", ArgumentTypes.world())
                    .requires(source -> source.getSender().hasPermission(commandPermission))
                    .then(Commands.argument("location", ArgumentTypes.blockPosition())
                        .executes(source -> new AdminTeleportCommand().teleportToLocation(source))
                    )
                )

                // Teleport to a player
                .then(Commands.argument("target", ArgumentTypes.player())
                    .requires(source -> source.getSender().hasPermission(commandPermission))
                    .executes(source -> new AdminTeleportCommand().teleportToOneTarget(source))
                );

        commands.register(
            commandBuilder.build(),
            "Admin teleport command",
            List.of("atp")
        );
    }

    public int teleportToOneTarget(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Entity entity = source.getExecutor();


        if (!(entity instanceof Player)) {
            Message.sendMessage(entity,"must_be_player");
            return 0;
        }

        Player player = (Player) entity;

        if (!player.hasPermission(commandPermission)) {
            Message.sendMessage(player, "no_permission");
            return 0;
        }

        if (!player.hasPermission("lyttleessentials.admintp.self")) {
            Message.sendMessage(player, "no_permission");
            return 0;
        }

        String targetName = context.getArgument("target", String.class);
        Player target = plugin.getServer().getPlayer(targetName);

        if (target == null) {
            Message.sendMessage(player, "player_not_found");
            return 0;
        }

        player.teleport(target);

        String[][] messageReplacements = {
            { "<USER>", getDisplayName(player) },
            { "<TARGET>", getDisplayName(target) }
        };
        Message.sendMessage(player, "atp_user", messageReplacements);
        return 0;
    }

    public int teleportSourceToTarget(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Entity entity = source.getExecutor();


        if (!(entity instanceof Player)) {
            Message.sendMessage(entity,"must_be_player");
            return 0;
        }

        Player player = (Player) entity;

        if (!player.hasPermission("lyttleessentials.admintp.other")) {
            Message.sendMessage(player, "no_permission");
            return 0;
        }

        String sourceTargetName = context.getArgument("source", String.class);
        Player sourceTarget = plugin.getServer().getPlayer(sourceTargetName);

        String targetName = context.getArgument("target", String.class);
        Player target = plugin.getServer().getPlayer(targetName);

        if (sourceTarget == null || target == null) {
            Message.sendMessage(player, "player_not_found");
            return 0;
        }

        sourceTarget.teleport(target);

        String[][] messageReplacements = {
            { "<USER>", getDisplayName(sourceTarget) },
            { "<TARGET>", getDisplayName(target) }
        };
        Message.sendMessage(player, "atp_user", messageReplacements);
        return 0;
    }

    public int teleportToLocation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Entity entity = source.getExecutor();


        if (!(entity instanceof Player)) {
            Message.sendMessage(entity,"must_be_player");
            return 0;
        }

        Player player = (Player) entity;
        BlockPosition location = context.getArgument("location", BlockPosition.class);
        String world = context.getArgument("world", String.class);

        Console.command("minecraft:execute as " +  player.getName() + " at @s in " + world + " run tp " + location.x() + " " + location.y() + " " + location.z());

        String[][] messageReplacements = {
                { "<USER>", getDisplayName(player) },
                { "<TARGET>", "Loc(" + location.x() + ", " + location.y() + ", " + location.z() + ")" }
        };
        Message.sendMessage(player, "atp_user", messageReplacements);
        return 0;
    }

    public int teleportSourceToLocation(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Entity entity = source.getExecutor();


        if (!(entity instanceof Player)) {
            Message.sendMessage(entity,"must_be_player");
            return 0;
        }

        Player player = (Player) entity;
        String userName = context.getArgument("source", String.class);
        Player user = plugin.getServer().getPlayer(userName);
        BlockPosition location = context.getArgument("location", BlockPosition.class);
        String world = context.getArgument("world", String.class);

        if (user == null) {
            Message.sendMessage(player, "player_not_found");
            return 0;
        }

        Console.command("minecraft:execute as " +  user.getName() + " at @s in " + world + " run tp " + location.x() + " " + location.y() + " " + location.z());

        String[][] messageReplacements = {
                { "<USER>", getDisplayName(user) },
                { "<TARGET>", "Loc(" + location.x() + ", " + location.y() + ", " + location.z() + ")" }
        };
        Message.sendMessage(player, "atp_user", messageReplacements);
        return 0;
    }
}
