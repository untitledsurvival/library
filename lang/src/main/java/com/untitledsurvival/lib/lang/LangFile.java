package com.untitledsurvival.lib.lang;

import com.untitledsurvival.lib.lang.placeholder.PlaceholderAPI;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class LangFile extends YamlConfiguration {
    @Getter private final Plugin plugin;

    public LangFile(@NonNull Plugin plugin) {
        super();

        this.plugin = plugin;
    }

    public LangFile(@NonNull Plugin plugin, @NonNull String name) {
        super();

        this.plugin = plugin;
        loadPluginResource(name);
    }

    @SneakyThrows
    public void loadPluginResource(@NonNull String name) {
        // if the file exists then load that instead
        File file = new File(plugin.getDataFolder(), name);
        if (file.exists()) {
            load(file);
        }

        InputStream resource = plugin.getResource(name);

        if (resource == null) {
            return; // no plugin resource
        }

        // load and save the resource to the provided file name
        load(new InputStreamReader(resource));
        save(file);
    }

    @Override
    public void save(@NonNull String file) throws IOException {
        save(new File(plugin.getDataFolder(), file));
    }

    public String format(String path, Object... format) {
        return format(false, path, format);
    }

    public String format(boolean preColor, String path, Object... format) {
        Object string = get(path);

        // if there is no path then send it back
        if (string == null) {
            return color(String.format(path, format));
        }

        // turn any message that might be multi-lined (aka list) into a bunch of lines
        //noinspection unchecked
        String lang = string instanceof List ? String.join("\n", (List<String>) string) : (String) string;

        // prevents injected color codes
        if (preColor) {
            return String.format(color(lang), format);
        }

        // send the colored string from config
        return color(String.format(lang, format));
    }

    public String pFormat(String path, Object... objects) {
        return pFormat(false, path, objects);
    }

    public String pFormat(boolean preColor, String path, Object... objects) {
        Object configString = get(path);

        // same logic as before, if there is no path then send it back
        if (configString == null) {
            return color(path);
        }

        //noinspection unchecked
        String string = configString instanceof List
                ? String.join("\n", (List<String>) configString)
                : (String) configString;

        if (preColor) {
            string = color(string);
        }

        // for all objects provided apply the placeholders for them
        string = PlaceholderAPI.apply(string, objects);

        // color the placeholder formatted string if necessary
        return preColor ? string : color(string);
    }

    public BaseComponent[] chatFormat(String path, Object... objects) {
        return chatFormat(true, path, objects);
    }

    public BaseComponent[] chatFormat(boolean placeBefore, String path, Object... objects) {
        return baseFormat(placeBefore, true, path, objects);
    }

    public BaseComponent[] altFormat(String path, Object... objects) {
        return altFormat(true, path, objects);
    }

    public BaseComponent[] altFormat(boolean placeBefore, String path, Object... objects) {
        return baseFormat(placeBefore, false, path, objects);
    }

    public BaseComponent[] baseFormat(boolean placeBefore, boolean useActions, String path, Object... objects) {
        Object configString = get(path);

        // same logic as before, if there is no path then send it back
        if (configString == null) {
            return colorC(path);
        }

        //noinspection unchecked
        String string = configString instanceof List
                ? String.join("\n", (List<String>) configString)
                : (String) configString;

        if (placeBefore) {
            // for all objects provided apply the placeholders for them
            string = PlaceholderAPI.apply(string, objects);
        }

        // get the parsed markdown of the path's String and return
        BaseComponent[] components = Markdown.parse(string, useActions);
        String json = ComponentSerializer.toString(components);

        return placeBefore ? components : ComponentSerializer.parse(PlaceholderAPI.apply(json, objects));
    }

    public static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static BaseComponent[] colorC(String input) {
        return Markdown.parse(input, false);
    }
}
