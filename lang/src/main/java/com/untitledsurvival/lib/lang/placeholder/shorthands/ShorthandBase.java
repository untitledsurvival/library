package com.untitledsurvival.lib.lang.placeholder.shorthands;

import com.untitledsurvival.lib.lang.placeholder.Placeholder;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class ShorthandBase implements Placeholder<Player> {
    @Getter private final Map<String, PlayerShorthand> standalones = new HashMap<>();

    @Override
    public String apply(Player player, String placeholderName) {
        // return the standalone result from the map
        return standalones.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(placeholderName))
                .map(entry -> entry.getValue().apply(player))
                .findFirst().orElse(null);
    }

    @Override
    public Class<Player> getType() {
        return Player.class;
    }
}
