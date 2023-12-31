package net.maplemc.lyttleessentials.commands;

import net.maplemc.lyttleessentials.LyttleEssentials;
import net.maplemc.lyttleessentials.types.SuccessfulTpaEvent;
import net.maplemc.lyttleessentials.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class TeleportCommandOld implements CommandExecutor, TabCompleter {
    private LyttleEssentials plugin;

    static HashMap<UUID, UUID> targetMap = new HashMap<>();

    public TeleportCommandOld(LyttleEssentials plugin) {
        plugin.getCommand("tp").setExecutor(this);
        plugin.getCommand("tpaccept_accept").setExecutor(this);
        plugin.getCommand("tpdeny").setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Message.sendConsole("must_be_player", Message.noReplacements);
            return true;
        }

        if (label.equalsIgnoreCase("tp")) {
            return tpCommand(sender, command, label, args);
        }

        if (label.equalsIgnoreCase("tpaccept_accept")) {
            return tpAcceptCommand(sender, command, label, args);
        }

        if (label.equalsIgnoreCase("tpdeny")) {
            return tpDenyCommand(sender, command, label, args);
        }

        return true;
    }

    public boolean tpCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        // Check if target player is online and found
        if (Bukkit.getPlayerExact(args[0]) == null) {
            Message.sendPlayer(player, "player_not_found", Message.noReplacements);
            return true;
        }

        // Stores some crucial info
        String playername = player.getName();
        Player playerTarget = Bukkit.getPlayer(args[0]);

        // Checks for a TP to the player himself
        if (player == playerTarget) {
            Message.sendPlayer(player, "to_self_teleport", Message.noReplacements);
            return true;
        }

        // Check if the player already has a /tp request
        if (targetMap.containsKey(player.getUniqueId())) {
            Message.sendPlayer(player, "already_request", Message.noReplacements);
            return true;
        }

        // get player scoreboard information
        Scoreboard board  = player.getScoreboard();
        Objective tokens = board.getObjective("tokens");
        Objective tpTokens = board.getObjective("tptokens");

        // translate player scoreboard data to int
        Score tokensScore = tokens.getScore(playername);
        Score tpTokensScore = tpTokens.getScore(playername);

        int tokensInt = tokensScore.getScore();
        int tpTokensInt = tpTokensScore.getScore();

        // define how many tokens would be taken off
        int tokensToSubtract = getPointsRequired(tpTokensInt);
        // define how many tokens the player would be left with
        int resultingTokenAmount = tokensInt - tokensToSubtract;

        // send a message if the player does not have enough tokens
        if (resultingTokenAmount < 0) {
            Message.sendPlayer(player, "too_poor", Message.noReplacements);
            return true;
        }

        String[][] replacements = {
                {"<PLAYER>", player.getDisplayName()}
        };
        Message.sendPlayer(playerTarget, "tp_ask_target", replacements);

        targetMap.put(player.getUniqueId(), playerTarget.getUniqueId());

        String[][] replacements2 = {
                {"<TARGET>", playerTarget.getDisplayName()}
        };
        Message.sendPlayer(player, "tp_requested", replacements2);

        // Wait 5 minutes to remove the teleport request
        (new BukkitRunnable() {
            public void run() {
                targetMap.remove(player.getUniqueId());
            }
        }).runTaskLaterAsynchronously((Plugin)this.plugin, 6000L);
        return true;
    }

    public boolean tpAcceptCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player player = (Player) sender;
        if (targetMap.containsValue(player.getUniqueId())) {
            Message.sendPlayer(player, "tpaccept_accept", Message.noReplacements);
            for (Map.Entry<UUID, UUID> entry : targetMap.entrySet()) {
                if (((UUID)entry.getValue()).equals(player.getUniqueId())) {

                    Player playerSender = Bukkit.getPlayer(entry.getKey());
                    String playername = playerSender.getName();

                    // get player scoreboard information
                    Scoreboard board  = playerSender.getScoreboard();
                    Objective tokens = board.getObjective("tokens");
                    Objective tpCount = board.getObjective("tpcount");
                    Objective tpTokens = board.getObjective("tptokens");

                    // translate player scoreboard data to int
                    Score tokensScore = tokens.getScore(playername);
                    Score tpCountScore = tpCount.getScore(playername);
                    Score tpTokensScore = tpTokens.getScore(playername);

                    int tokensInt = tokensScore.getScore();
                    int tpCountInt = tpCountScore.getScore();
                    int tpTokensInt = tpTokensScore.getScore();

                    // define how many tokens would be taken off
                    int tokensToSubtract = getPointsRequired(tpTokensInt);
                    // define how many tokens the player would be left with
                    int resultingTokenAmount = tokensInt - tokensToSubtract;

                    // get server scoreboard information
                    Score serverTokens = board.getObjective("tokens").getScore("#server");
                    Score serverTpTokens = board.getObjective("tptokens").getScore("#server");

                    // translate server scoreboard data to int
                    int serverTokensInt = serverTokens.getScore();
                    int serverTpTokensInt = serverTpTokens.getScore();

                    // actually apply token subtraction
                    tokensScore.setScore(resultingTokenAmount);
                    // increment tpCount
                    tpCountInt++;
                    tpCountScore.setScore(tpCountInt);
                    // statistics
                    serverTokensInt = serverTokensInt + tokensToSubtract;
                    serverTokens.setScore(serverTokensInt);
                    tpTokensInt = tokensToSubtract;
                    tpTokensScore.setScore(tpTokensInt);
                    serverTpTokensInt = serverTpTokensInt + tokensToSubtract;
                    serverTpTokens.setScore(serverTpTokensInt);

                    SuccessfulTpaEvent event = new SuccessfulTpaEvent(playerSender, playerSender.getLocation());
                    Bukkit.getPluginManager().callEvent(event);

                    //send message with info to the player
                    int costNextTime = getPointsRequired(tpTokensInt);
                    String[][] replacements = {
                            {"<CostNow>", String.valueOf(tokensToSubtract)},
                            {"<CostNextTime>", String.valueOf(costNextTime)}
                    };
                    Message.sendPlayer(playerSender, "tp_teleporting", replacements);

                    playerSender.teleport((Entity)player);
                    targetMap.remove(entry.getKey());
                    break;
                }
            }
        } else {
            Message.sendPlayer(player, "tpaccept_no_request", Message.noReplacements);
        }
        return true;
    }

    public boolean tpDenyCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player player = (Player) sender;
        if (targetMap.containsValue(player.getUniqueId())) {
            for (Map.Entry<UUID, UUID> entry : targetMap.entrySet()) {
                if (((UUID)entry.getValue()).equals(player.getUniqueId())) {
                    targetMap.remove(entry.getKey());
                    Player playerSender = Bukkit.getPlayer(entry.getKey());
                    String[][] replacements = {
                            {"<TARGET>", player.getDisplayName()}
                    };
                    Message.sendPlayer(playerSender, "tpdeny_denied_player", replacements);


                    String[][] replacements2 = {
                            {"<TARGET>", player.getDisplayName()}
                    };
                    Message.sendPlayer(playerSender, "tpdeny_denied_player", replacements2);
                    Message.sendPlayer(player, "tpdeny_denied_target", Message.noReplacements);
                    break;
                }
            }
        } else {
            Message.sendPlayer(player, "tpaccept_no_request", Message.noReplacements);
        }
        return true;
    }


    // Math for TP costs
    private int getPointsRequired(int i) {
        int pointsRequired;
        pointsRequired = i + 2;
        return pointsRequired;
    }

    // Checks if Costs should be reset
    public static void onPlayerJoin(PlayerJoinEvent event){
        // Stores the player info
        Player player = event.getPlayer();
        String playername = player.getName();
        // Stores system time in an int
        String SystemDate = (String.valueOf(java.time.LocalDate.now()).replace("-", ""));
        int SystemDateInt = Integer.parseInt(SystemDate);
        // Gets player scoreboard data
        Scoreboard board  = player.getScoreboard();
        Objective tpDate = board.getObjective("tpdate");
        Objective tpCount = board.getObjective("tpcount");
        Objective tpTokens = board.getObjective("tptokens");
        // Gets player scoreboard objectives
        Score tpCountScore = tpCount.getScore(playername);
        Score tpTokensScore = tpTokens.getScore(playername);
        Score tpDateScore = tpDate.getScore(playername);
        // Stores the most essential
        int tpDateInt = tpDateScore.getScore();
        // Does the actual math and reset if necessary
        if (SystemDateInt != tpDateInt) {
            tpDateInt = SystemDateInt;
            tpDateScore.setScore(tpDateInt);
            tpTokensScore.setScore(0);
            tpCountScore.setScore(0);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] arguments) {
        if (arguments.length == 1) {
            return null;
        }

        return Arrays.asList();
    }
}
