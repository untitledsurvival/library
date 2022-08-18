package com.untitledsurvival.lib.scoreboard;

import com.untitledsurvival.lib.LibModule;
import com.untitledsurvival.lib.UntitledLib;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class ScoreboardAPI extends LibModule implements Listener {
    @Getter
    private static final Map<UUID, PlayerScoreboard> playerScoreboards = new HashMap<>();

    @Override
    public void init() {
        Bukkit.getPluginManager().registerEvents(this, UntitledLib.getPlugin());
    }

    @Override
    public void deinit() {
        playerScoreboards.clear();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerScoreboards.remove(event.getPlayer().getUniqueId());
    }

    public static PlayerScoreboard scoreboardFor(Player player) {
        return playerScoreboards.getOrDefault(player.getUniqueId(), new PlayerScoreboard(player));
    }
}
