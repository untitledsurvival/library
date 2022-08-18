package com.untitledsurvival.lib.lang.placeholder.builtin.wildcard;

import com.untitledsurvival.lib.lang.placeholder.Placeholder;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.util.concurrent.atomic.AtomicInteger;

public class ServerPlaceholder implements Placeholder.Wildcard {
    @Override
    public String apply(@NonNull String placeholderName) {
        Server server = Bukkit.getServer();

        return switch (placeholderName) {
            case "name" -> server.getName();
            case "motd" -> server.getMotd();

            case "online" -> "%,d".formatted(server.getOnlinePlayers().size());
            case "max_online" -> "%,d".formatted(server.getMaxPlayers());

            case "advances" -> {
                AtomicInteger count = new AtomicInteger(0);
                Bukkit.advancementIterator().forEachRemaining(ignored -> count.incrementAndGet());

                yield "%,d".formatted(count.get());
            }

            case "world_count" -> "%,d".formatted(server.getWorlds().size());

            default -> null;
        };
    }

    @Override
    public String getNamespace() {
        return "server";
    }
}
