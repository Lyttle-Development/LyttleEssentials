package com.lyttledev.lyttleessentials.commands;


import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class TopCommand {
    private static LyttleEssentials plugin;

    public static void createCommand(LyttleEssentials lyttlePlugin, Commands commands) {
        plugin = lyttlePlugin;

        // Define the different nodes
        LiteralArgumentBuilder<CommandSourceStack> top = Commands.literal("top")
            .then(Commands.argument("player", ArgumentTypes.players())
                .requires(source -> source.getSender().hasPermission("lyttleessentials.top.other"))
                .executes(TopCommand::targetNode));


        // Defines root node functions
        top.requires(source -> source.getSender().hasPermission("lyttleessentials.top"));
        top.executes(TopCommand::rootNode);

        // Finish the command
        commands.register(
            top.build(),
            "Teleport to the top block at a location"
        );
    }

    private static int rootNode(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player)) {
            plugin.message.sendMessage(sender,"top_usage");
            return Command.SINGLE_SUCCESS;
        }
        topSelf((Player) sender);
        return Command.SINGLE_SUCCESS;
    }

    private static int targetNode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        final PlayerSelectorArgumentResolver resolver = context.getArgument("player", PlayerSelectorArgumentResolver.class);
        final List<Player> targets = resolver.resolve(context.getSource());
        final CommandSender sender = context.getSource().getSender();

        for (Player target : targets) {
            if (target == sender) {
                topSelf(target);
            } else  {
                topOther(sender, target);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static void topSelf(Player player) {
        plugin.message.sendMessage(player,"top_self");
        teleportToTop(player);
    }

    private static void topOther(CommandSender sender, Player target) {
        if (sender instanceof Player) {
            Replacements replacementsTarget = new Replacements.Builder()
                    .add("<PLAYER>", getDisplayName((Player) sender))
                    .build();
            plugin.message.sendMessage(target,"top_other_target", replacementsTarget);
        } else {
            plugin.message.sendMessage(target,"top_console");
        }
        Replacements replacementsSender = new Replacements.Builder()
                .add("<PLAYER>", getDisplayName(target))
                .build();
        plugin.message.sendMessage(sender,"top_other_sender", replacementsSender);
        teleportToTop(target);
    }

    private static void teleportToTop(Player player) {
        player.teleport(player.getWorld().getHighestBlockAt(player.getLocation()).getLocation().add(0, 1, 0));
    }

}
