package com.untitledsurvival.lib.scoreboard;

import com.untitledsurvival.lib.LibModule;
import com.untitledsurvival.lib.UntitledLib;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScoreboardAPI extends LibModule implements Listener {
    @Getter private static final List<PlayerScoreboard> scoreboards = new ArrayList<>();

    @Override
    public void init() {
        Bukkit.getPluginManager().registerEvents(this, UntitledLib.getPlugin());
    }

    @Override
    public void deinit() {
        scoreboards.clear();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeFromScoreboards(event.getPlayer());
    }

    public static PlayerScoreboard scoreboardFor(Player player) {
        return scoreboardFor(player, true);
    }

    public static PlayerScoreboard scoreboardFor(Player player, boolean cache) {
        // loop through using lambdas and find the proper one
        // if necessary creating a new one and caching it
        return scoreboards.stream()
                .filter(sb -> sb.getPlayerUUID().equals(player.getUniqueId()))
                .findFirst().or(() -> {
                    PlayerScoreboard scoreboard = new PlayerScoreboard(player);
                    if (cache) scoreboards.add(scoreboard);

                    return Optional.of(scoreboard);
                }).orElse(null);
    }

    public static boolean playerHasScoreboard(Player player) {
        return scoreboards.stream().anyMatch(scoreboard -> player.getUniqueId().equals(scoreboard.getPlayerUUID()));
    }

    public static void removeFromScoreboards(Player player) {
        scoreboards.removeIf(scoreboard -> player.getUniqueId().equals(scoreboard.getPlayerUUID()));
    }

    public static ChatColor getColorByLine(int line) {
        if (line < 0 || line > ChatColor.values().length) {
            return null;
        }

        // return the chat color based on the line number (max 22)
        return ChatColor.values()[line];
    }

    public static String getTeamNameByLine(int line) {
        return "line" + line;
    }
}
