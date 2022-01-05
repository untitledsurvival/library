package com.untitledsurvival.lib.plugin.utils;

import com.untitledsurvival.lib.LibModule;
import com.untitledsurvival.lib.UntitledLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * CAUTION:
 *  This class is meant to prevent MEMORY LEAKS, this class should
 *  ONLY EVER be under a static final field
 */
public class OnlinePlayerMap<T> extends HashMap<UUID, T> {
    public OnlinePlayerMap() {
        super();

        // add this map to the PLAYER_MAPS array
        PlayerMapModule.PLAYER_MAPS.add(this);
    }

    public T put(Player key, T value) {
        return super.put(key.getUniqueId(), value);
    }

    public boolean containsKey(Player key) {
        return super.containsKey(key.getUniqueId());
    }

    public T get(Player key) {
        return super.get(key.getUniqueId());
    }

    public T getOrDefault(Player key, T defaultValue) {
        return super.getOrDefault(key.getUniqueId(), defaultValue);
    }

    public T remove(Player player) {
        return super.remove(player.getUniqueId());
    }

    public static class PlayerMapModule extends LibModule implements Listener {
        public static final List<OnlinePlayerMap<?>> PLAYER_MAPS = new ArrayList<>();

        @Override
        public void init() {
            Bukkit.getPluginManager().registerEvents(this, UntitledLib.getPlugin());
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            PLAYER_MAPS.forEach(map -> map.remove(event.getPlayer()));
        }
    }
}
