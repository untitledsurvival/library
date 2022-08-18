package com.untitledsurvival.lib.lang.utils;

import com.untitledsurvival.lib.lang.markdown.Markdown;
import com.untitledsurvival.lib.lang.placeholder.PlaceholderAPI;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class MessageFormatter {
    @Getter private final List<CommandSender> recipients = new ArrayList<>();
    private final PlaceholderAPI placeholderAPI;

    @Getter private BaseComponent[] components;

    MessageFormatter(String message) {
        this.placeholderAPI = new PlaceholderAPI();
        this.components = new BaseComponent[] { new TextComponent(message) };
    }

    public MessageFormatter constant(String name, String value) {
        placeholderAPI.addConstant(name, value);
        return this;
    }

    public MessageFormatter md() {
        return this.md(true);
    }

    public MessageFormatter md(boolean useActions) {
        this.components = Markdown.parse(this.toString(), useActions);
        return this;
    }

    public MessageFormatter format(Object... objs) {
        // for each of the components, apply placeholders to their text
        return this.map(component -> component.setText(placeholderAPI.apply(component.getText(), objs)));
    }

    public MessageFormatter color() {
        return this.map(component -> component.setText(ChatColor.translateAlternateColorCodes('&', component.getText())));
    }

    public MessageFormatter map(Consumer<TextComponent> componentModifier) {
        for (BaseComponent component : this.components) {
            if (!(component instanceof TextComponent textComponent)) {
                continue;
            }

            componentModifier.accept(textComponent);
        }

        return this;
    }

    public String toString() {
        return BaseComponent.toPlainText(this.components);
    }

    public List<String> asLines() {
        return List.of(toString().split("\n"));
    }

    public String serialize() {
        // turn an array of components into its json format
        return ComponentSerializer.toString(this.components);
    }

    public MessageFormatter send(Collection<? extends Player> recipients) {
        for (CommandSender recipient : recipients) {
            this.send(recipient);
        }

        return this;
    }

    public MessageFormatter send(CommandSender... recipients) {
        for (CommandSender recipient : recipients) {
            recipient.spigot().sendMessage(this.components);
            this.recipients.add(recipient);
        }

        return this;
    }
}
