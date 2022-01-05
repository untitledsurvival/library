package com.untitledsurvival.lib.plugin.commands;

import com.untitledsurvival.lib.lang.markdown.Markdown;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MarkdownTest implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String input = String.join(" ", args);
        input = input.replace("\\n", "\n");

        long before = System.nanoTime();
        BaseComponent[] parsed = Markdown.parse(input);
        long after  = System.nanoTime();

        sender.spigot().sendMessage(parsed);
        sender.sendMessage(ComponentSerializer.toString(parsed));

        long diff = after - before;
        sender.sendMessage("Performed in %,d ns (%,.3f ms)".formatted(after - before, diff * 0.000_001));

        return true;
    }
}
