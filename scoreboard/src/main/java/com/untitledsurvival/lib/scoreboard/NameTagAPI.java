package com.untitledsurvival.lib.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class NameTagAPI {
    public static void setNameTag(Player player, String name) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getMainScoreboard();

        scoreboard.registerNewTeam(player.getUniqueId().toString()).setPrefix("CUSTOM ");

        player.setCustomName(name);
        player.setCustomNameVisible(true);
    }
}
