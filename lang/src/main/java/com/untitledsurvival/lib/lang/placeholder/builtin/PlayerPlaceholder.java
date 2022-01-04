package com.untitledsurvival.lib.lang.placeholder.builtin;

import com.untitledsurvival.lib.lang.placeholder.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class PlayerPlaceholder implements Placeholder<Player> {
    @Override
    public String apply(Player player, String placeholderName) {
        return switch (placeholderName) {
            case "name" -> player.getName();
            case "gamemode" -> player.getGameMode().toString();
            case "level" -> String.valueOf(player.getExpToLevel());

            case "advance_count" -> {
                AtomicInteger count = new AtomicInteger(0);

                Bukkit.getServer().advancementIterator().forEachRemaining(advancement -> {
                    if (player.getAdvancementProgress(advancement).isDone()) {
                        count.getAndIncrement();
                    }
                });

                yield "%,d".formatted(count.get());
            }

            case "ping" -> "%,d".formatted(player.getPing());

            default -> null;
        };
    }

    @Override
    public String getNamespace() {
        return "player";
    }

    @Override
    public Class<Player> getType() {
        return Player.class;
    }
}
