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
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.List;

import static com.lyttledev.lyttleessentials.utils.DisplayName.getDisplayName;

public class HealCommand {
    private static LyttleEssentials plugin;

    public static void createCommand(LyttleEssentials lyttlePlugin, Commands commands) {
        plugin = lyttlePlugin;

        // Define the different nodes
        LiteralArgumentBuilder<CommandSourceStack> heal = Commands.literal("heal")
                .then(Commands.argument("player", ArgumentTypes.players())
                        .requires(source -> source.getSender().hasPermission("lyttleessentials.heal.other"))
                        .executes(HealCommand::targetNode));

        // Defines root node functions
        heal.requires(source -> source.getSender().hasPermission("lyttleessentials.heal.self"));
        heal.executes(HealCommand::rootNode);

        // Finish the command
        commands.register(
                heal.build(),
                "Heal someone"
        );
    }

    private static int rootNode(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player)) {
            plugin.message.sendMessage(sender,"heal_usage");
            return Command.SINGLE_SUCCESS;
        }
        healSelf((Player) sender);
        return Command.SINGLE_SUCCESS;
    }

    private static int targetNode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver resolver = context.getArgument("player", PlayerSelectorArgumentResolver.class);
        final List<Player> targets = resolver.resolve(context.getSource());
        final CommandSender sender = context.getSource().getSender();

        for (Player target : targets) {
            if (target == sender) {
                healSelf(target);
            } else  {
                healOther(sender, target);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static void healSelf(Player player) {
        plugin.message.sendMessage(player,"heal_self");
        heal(player);
    }

    private static void healOther(CommandSender sender, Player target) {
        if (sender instanceof Player) {
            Replacements replacementsTarget = new Replacements.Builder()
                    .add("<PLAYER>", getDisplayName((Player) sender))
                    .build();
            plugin.message.sendMessage(target,"heal_other_target", replacementsTarget);
        } else {
            plugin.message.sendMessage(target,"heal_console");
        }
        Replacements replacementsSender = new Replacements.Builder()
                .add("<PLAYER>", getDisplayName(target))
                .build();
        plugin.message.sendMessage(sender,"heal_other_sender", replacementsSender);
        heal(target);
    }

    private static void heal(Player player) {
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        for (PotionEffect effect : player.getActivePotionEffects()) { player.removePotionEffect(effect.getType()); }
    }
}
