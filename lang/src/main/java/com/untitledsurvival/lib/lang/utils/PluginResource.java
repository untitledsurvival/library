package com.untitledsurvival.lib.lang.utils;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.file.NoSuchFileException;

public class PluginResource extends YamlConfiguration {
    @Getter private final Plugin plugin;
    private final ScopedFormatter globalScope = new ScopedFormatter(this);

    public PluginResource(@NonNull Plugin plugin) {
        super();

        this.plugin = plugin;
    }

    public PluginResource(@NonNull Plugin plugin, @NonNull String name) {
        super();

        this.plugin = plugin;
        this.loadPluginResource(name);
    }

    @SneakyThrows
    public void loadPluginResource(@NonNull String name) {
        // if the file exists then load that instead
        File file = new File(plugin.getDataFolder(), name);
        InputStream resource = plugin.getResource(name);

        // make sure the plugin resource is valid
        if (resource == null) {
            throw new NoSuchFileException("Could not find resource: %s".formatted(name));
        }

        // set the defaults to the configuration in the plugin
        setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(resource)));

        // save the file in its current state
        if (!file.exists()) {
            write(name, file);
            return;
        }

        // load the resource
        load(file);

        // set copy defaults to true and save the file
        options.copyDefaults(true);
        save(file);
    }

    @Override
    public void save(@NonNull String file) throws IOException {
        save(new File(plugin.getDataFolder(), file));
    }

    @SneakyThrows
    public void write(String resourceName, @NonNull File file) {
        // create parent directories
        if (!file.getParentFile().mkdirs()) {
            return;
        }

        // write the file from the plugin directly
        plugin.getResource(resourceName).transferTo(new FileOutputStream(file));
    }

    public MessageFormatter fmt(@NonNull String key) {
        return this.globalScope.fmt(key);
    }

    public ScopedFormatter scope(@NonNull String key) {
        return new ScopedFormatter(this, key);
    }
}
