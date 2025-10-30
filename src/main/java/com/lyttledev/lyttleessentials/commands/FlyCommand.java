package com.lyttledev.lyttleessentials.commands;

import com.lyttledev.lyttleessentials.LyttleEssentials;
import com.lyttledev.lyttleutils.types.Message.Replacements;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class FlyCommand {
    private static LyttleEssentials plugin;

    public static void createCommand(LyttleEssentials lyttlePlugin, Commands commands) {
        plugin = lyttlePlugin;

        LiteralArgumentBuilder<CommandSourceStack> fly = Commands.literal("fly")
            .requires(source -> source.getSender().hasPermission("lyttleessentials.fly.self"))
            .executes(FlyCommand::rootNode)
                .then(Commands.argument("flag", BoolArgumentType.bool())
                    .requires(source -> source.getSender().hasPermission("lyttleessentials.fly.self"))
                    .executes(FlyCommand::flagNode))
                .then(Commands.argument("player", ArgumentTypes.player())
                    .requires(source -> source.getSender().hasPermission("lyttleessentials.fly.other"))
                    .executes(FlyCommand::targetNode)
                    .then(Commands.argument("flag", BoolArgumentType.bool())
                        .requires(source -> source.getSender().hasPermission("lyttleessentials.fly.other"))
                        .executes(FlyCommand::targetFlagNode)));


        // Finish the command
        commands.register(
            fly.build(),
            "Teleport to the top block at a location"
        );
    }

    private static int rootNode(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player)) {
            plugin.message.sendMessage(sender,"fly_usage");
            return Command.SINGLE_SUCCESS;
        }
        boolean active = toggleFly((Player) sender);
        flySelfMessage((Player) sender, active);
        return Command.SINGLE_SUCCESS;
    }

    private static int flagNode(CommandContext<CommandSourceStack> context) {
        boolean flag = context.getArgument("flag", boolean.class);
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player)) {
            plugin.message.sendMessage(sender,"fly_usage");
            return Command.SINGLE_SUCCESS;
        }
        setFly((Player) sender, flag);
        flySelfMessage((Player) sender, flag);
        return Command.SINGLE_SUCCESS;
    }

    private static int targetNode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver resolver = context.getArgument("player", PlayerSelectorArgumentResolver.class);
        final List<Player> targets = resolver.resolve(context.getSource());
        final CommandSender sender = context.getSource().getSender();

        for (Player target : targets) {
            if (target == sender) {
                boolean active = toggleFly(target);
                flySelfMessage(target, active);
            } else  {
                boolean active = toggleFly(target);
                flyMessage(sender, target, active);
                return Command.SINGLE_SUCCESS;
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int targetFlagNode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver resolver = context.getArgument("player", PlayerSelectorArgumentResolver.class);
        final List<Player> targets = resolver.resolve(context.getSource());
        final CommandSender sender = context.getSource().getSender();
        boolean flag = context.getArgument("flag", boolean.class);

        for (Player target : targets) {
            if (target == sender) {
                setFly(target, flag);
                flySelfMessage(target, flag);
            } else {
                setFly(target, flag);
                flyMessage(sender, target, flag);
                return Command.SINGLE_SUCCESS;
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static boolean toggleFly(Player player) {
        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
            return false;
        }
        player.setAllowFlight(true);
        return true;
    }

    private static void setFly(Player player,  boolean flag) {
        player.setAllowFlight(flag);
    }

    private static void flySelfMessage(Player target, boolean active) {
        if (active) {
            plugin.message.sendMessage(target, "fly_activate");
            return;
        }
        plugin.message.sendMessage(target, "fly_deactivate");
    }

    private static void flyMessage(CommandSender sender, Player target, boolean active) {
        Replacements replacementsSender = new Replacements.Builder()
                .add("<PLAYER>", getDisplayName(target))
                .build();

        if (sender instanceof Player) {
            Replacements replacementsPlayer = new Replacements.Builder()
                    .add("<PLAYER>", getDisplayName((Player) sender))
                    .build();

            if (active) {
                plugin.message.sendMessage(sender, "fly_activate_other_sender", replacementsSender);
                plugin.message.sendMessage(target, "fly_activate_other_target", replacementsPlayer);
                return;
            }
            plugin.message.sendMessage(sender, "fly_deactivate_other_sender", replacementsSender);
            plugin.message.sendMessage(target, "fly_deactivate_other_target", replacementsPlayer);
            return;
        }

        if (active) {
            plugin.message.sendMessage(sender, "fly_activate_other_sender", replacementsSender);
            plugin.message.sendMessage(target, "fly_activate_console");
            return;
        }
        plugin.message.sendMessage(sender, "fly_deactivate_other_sender", replacementsSender);
        plugin.message.sendMessage(target, "fly_deactivate_console");
    }


}
