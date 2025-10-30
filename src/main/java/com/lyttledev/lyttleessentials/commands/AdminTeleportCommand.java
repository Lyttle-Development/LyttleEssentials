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

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class AdminTeleportCommand {
    private static LyttleEssentials plugin;

    public static void createCommand(LyttleEssentials lyttlePlugin, Commands commands) {
        plugin = lyttlePlugin;

        LiteralArgumentBuilder<CommandSourceStack> fly = Commands.literal("fly")
            .requires(source -> source.getSender().hasPermission("lyttleessentials.admintp"))
            .executes(AdminTeleportCommand::rootNode)
            .then(Commands.argument("player", ArgumentTypes.player())
                .requires(source -> source.getSender().hasPermission("lyttleessentials.admintp.self"))
                .executes(AdminTeleportCommand::selfNode))
                    .then(Commands.argument("player", ArgumentTypes.player())
                        .requires(source -> source.getSender().hasPermission("lyttleessentials.admintp.other"))
                        .executes(AdminTeleportCommand::otherNode));


        // Finish the command
        commands.register(
                fly.build(),
                "Toggle a player's fly"
        );
    }

    public static int rootNode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getSender();
        plugin.message.sendMessage(sender, "atp_usage");
        return Command.SINGLE_SUCCESS;
    }

    public static int selfNode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getSender();

        if (!(sender instanceof Player)) {
            plugin.message.sendMessage(sender, "atp_usage");
            return Command.SINGLE_SUCCESS;
        }

        Player player = (Player) sender;
        PlayerSelectorArgumentResolver resolver = context.getArgument("player", PlayerSelectorArgumentResolver.class);
        Player target = resolver.resolve(context.getSource()).getFirst();
        player.teleport(target);

        Replacements replacementsPlayer = new Replacements.Builder()
                .add("<TARGET>", getDisplayName(target))
                .build();

        plugin.message.sendMessage(player, "atp_user", replacementsPlayer);

        Replacements replacementsTarget = new Replacements.Builder()
                .add("<PLAYER>", getDisplayName(player))
                .build();

        plugin.message.sendMessage(target, "atp_target", replacementsTarget);
        return Command.SINGLE_SUCCESS;
    }

    public static int otherNode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getSender();
        PlayerSelectorArgumentResolver resolver = context.getArgument("player", PlayerSelectorArgumentResolver.class);
        Player player = resolver.resolve(context.getSource()).getFirst();
        Player target = resolver.resolve(context.getSource()).get(1);
        player.teleport(target);

        Replacements replacementsUser = new Replacements.Builder()
                .add("<PLAYER>", getDisplayName(player))
                .add("<TARGET>", getDisplayName(target))
                .build();

        plugin.message.sendMessage(sender, "atp_user_other", replacementsUser);

        if (sender instanceof Player) {
            Replacements replacementsTarget = new Replacements.Builder()
                    .add("<USER>", getDisplayName((Player) sender))
                    .add("<PLAYER>", getDisplayName(player))
                    .build();

            plugin.message.sendMessage(target, "atp_target_other", replacementsTarget);

            Replacements replacementsPlayer = new Replacements.Builder()
                    .add("<USER>", getDisplayName((Player) sender))
                    .add("<TARGET>", getDisplayName(target))
                    .build();

            plugin.message.sendMessage(player, "atp_player_other", replacementsPlayer);
        } else {
            Replacements replacementsTarget = new Replacements.Builder()
                    .add("<PLAYER>", getDisplayName(player))
                    .build();

            plugin.message.sendMessage(target, "atp_console_target", replacementsTarget);

            Replacements replacementsPlayer = new Replacements.Builder()
                    .add("<TARGET>", getDisplayName(target))
                    .build();

            plugin.message.sendMessage(player, "atp_console_player", replacementsPlayer);
        }

        return Command.SINGLE_SUCCESS;
    }

}
