package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LyttleEssentialsCommand {
    private static LyttleEssentials plugin;

    public static void createCommand(LyttleEssentials lyttlePlugin, Commands commands) {
        plugin = lyttlePlugin;

        // Define the different nodes
        LiteralArgumentBuilder<CommandSourceStack> top = Commands.literal("lyttleessentials")
                .then(Commands.literal("reload")
                        .requires(source -> source.getSender().hasPermission("lyttleessentials.lyttleessentials.reload"))
                        .executes(LyttleEssentialsCommand::reloadNode));

        // Defines root node functions
        top.requires(source -> source.getSender().hasPermission("lyttleessentials.lyttleEssentials"));
        top.executes(LyttleEssentialsCommand::rootNode);

        // Finish the command
        commands.register(
                top.build(),
                "Teleport to the top block at a location"
        );
    }

    private static int rootNode(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        Component version = Component.text("Plugin version: " + plugin.getDescription().getVersion());
        plugin.message.sendMessageRaw(sender, version);
        return Command.SINGLE_SUCCESS;
    }

    private static int reloadNode(CommandContext<CommandSourceStack> context) {
        final CommandSender sender = context.getSource().getSender();
        plugin.config.reload();
        plugin.message.sendMessageRaw(sender, Component.text("The config has been reloaded"));
        return Command.SINGLE_SUCCESS;
    }
}
