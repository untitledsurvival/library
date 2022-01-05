package com.untitledsurvival.lib;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class UntitledLib {
    @Getter private static final List<LibModule> modules = new ArrayList<>();
    @Getter private static Plugin plugin = null;

    public static void setPlugin(@NonNull Plugin plugin) {
        if (UntitledLib.plugin != null) {
            return; // if a plugin is already registered don't override it
        }

        // set the plugin to the active one and call on all the modules
        // to be initialized
        UntitledLib.plugin = plugin;
        enableAll();
    }

    public static void register(@NonNull LibModule module) {
        modules.add(module);
        enableAll();
    }

    private static void enableAll() {
        if (plugin == null) {
            return; // do not enable all modules if plugin is null, some may require it
        }

        modules.forEach(LibModule::initialize);
    }

    private static void disableAll() {
        modules.forEach(LibModule::deinitialize);
    }

    public static void onPluginDisable() {
        disableAll();
    }
}
