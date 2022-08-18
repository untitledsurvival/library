package com.untitledsurvival.lib.plugin.commands;

import com.untitledsurvival.lib.lang.utils.PluginResource;
import com.untitledsurvival.lib.plugin.UntitledLibPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigManager implements CommandExecutor, TabCompleter {
    private void sendUsage(CommandSender sender, Command command) {
        UntitledLibPlugin.getLang().fmt("usage").md()
                .constant("usage", command.getUsage()).format(sender)
                .send(sender);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PluginResource lang = UntitledLibPlugin.getLang();

        if (args.length <= 1) {
            sendUsage(sender, command);
            return true;
        }

        String pluginName = args[0];
        String subCommand = args[1].toLowerCase();

        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin == null) {
            lang.fmt("not_found").md()
                    .constant("plugin", pluginName)
                    .format(sender).send(sender);
            return true;
        }

        FileConfiguration config = plugin.getConfig();

        switch (subCommand) {
            case "reload" -> {
                // reload the config
                plugin.reloadConfig();
                lang.fmt("commands.config.reloaded").md()
                        .constant("plugin", pluginName)
                        .format(sender).send(sender);
                return true;
            }

            case "update" -> {
                updateConfig(sender, command, args, config);
                return true;
            }

            case "save" -> {
                // save the config
                plugin.saveConfig();
                lang.fmt("commands.config.saved").md()
                        .constant("plugin", pluginName)
                        .format(sender).send(sender);
                return true;
            }

            default -> {
            }
        }

        // if we make it this far, send the usage message
        sendUsage(sender, command);

        return true;
    }

    private void updateConfig(CommandSender sender, Command command, String[] args, FileConfiguration config) {
        PluginResource lang = UntitledLibPlugin.getLang();

        // if they did not specify a path then send them the usage
        if (args.length < 3) {
            sendUsage(sender, command);
            return;
        }

        String path = args[1];

        Object value;

        // null check the path entered, it could be wrong (or not exist)
        if ((value = config.get(path)) == null || value instanceof ConfigurationSection) {
            lang.fmt("commands.config.invalid").md()
                    .constant("path", path)
                    .format(sender).send(sender);

            return;
        }

        // if there is no value to update it to, then send the current one
        if (args.length < 4) {
            lang.fmt("commands.config.current").md()
                    .constant("path", path)
                    .constant("value", value.toString())
                    .format(sender).send(sender);
            return;
        }

        // trim the args array down to miss the first 2 values
        String[] trimmed = new String[args.length - 2];
        System.arraycopy(args, 2, trimmed, 0, args.length - 2);

        String newValue = String.join(" ", trimmed);
        Object parsed   = parseInput(value.getClass(), newValue);

        // if null then the expected datatype was not met
        if (parsed == null) {
            lang.fmt("commands.config.invalid").md()
                    .constant("path", path)
                    .constant("value", newValue)
                    .constant("datatype", value.getClass().getSimpleName())
                    .format(sender).send(sender);
            return;
        }

        // update the value in the config
        // do not save
        config.set(path, parsed);

        lang.fmt("commands.config.updated").md()
                .constant("path", path)
                .constant("value", newValue)
                .format(sender).send(sender);
    }

    private Object parseInput(Class<?> clazzType, String newValue) {
        // note: could use reflection, decided not to, fuck that.

        if (Integer.class.equals(clazzType)) return parseOrFail(Integer::parseInt, newValue);
        else if (Double.class.equals(clazzType))  return parseOrFail(Double::parseDouble, newValue);
        else if (Float.class.equals(clazzType))   return parseOrFail(Float::parseFloat, newValue);
        else if (Short.class.equals(clazzType))   return parseOrFail(Short::parseShort, newValue);
        else if (Boolean.class.equals(clazzType)) return parseOrFail(Boolean::parseBoolean, newValue);

        // return the string object as default
        return newValue;
    }

    private Object parseOrFail(Function<String, Object> parseFunc, String toParse) {
        try {
            return parseFunc.apply(toParse);
        } catch (Exception ignored) {
        }

        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (args.length == 1) {
            return Arrays.stream(pluginManager.getPlugins()).map(Plugin::getName).collect(Collectors.toList());
        }

        if (args.length == 2) {
            return Arrays.asList("reload", "update", "save");
        }

        Plugin plugin = pluginManager.getPlugin(args[0]);
        if (plugin == null) {
            return Collections.emptyList();
        }

        // autocompletion for the config path
        if (args.length == 3 && args[1].equalsIgnoreCase("update")) {
            String path = args[2];
            int lastDot = path.lastIndexOf(".");

            String child = lastDot == -1 ? path : path.substring(lastDot + 1);
            String parentSection = lastDot == -1 ? "" : path.substring(0, lastDot);

            ConfigurationSection section = plugin.getConfig().getConfigurationSection(parentSection);
            // if null, this is furthest the path will go
            if (section == null) {
                return Collections.emptyList();
            }

            // return all paths that match the current path
            return section.getKeys(false).stream()
                    .filter(key -> key.startsWith(child))
                    .map(key -> (parentSection.isEmpty() ? "" : parentSection + ".") + key)
                    .toList();
        }

        // always empty, null provides a player list
        return Collections.emptyList();
    }
}
