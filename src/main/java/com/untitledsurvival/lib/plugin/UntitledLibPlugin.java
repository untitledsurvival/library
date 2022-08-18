package com.untitledsurvival.lib.plugin;

import com.untitledsurvival.lib.UntitledLib;
import com.untitledsurvival.lib.lang.utils.PluginResource;
import com.untitledsurvival.lib.plugin.commands.ConfigManager;
import com.untitledsurvival.lib.plugin.commands.MarkdownTest;
import com.untitledsurvival.lib.plugin.utils.OnlinePlayerMap;
import com.untitledsurvival.lib.scoreboard.ScoreboardAPI;
import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class UntitledLibPlugin extends JavaPlugin {
    @Getter private static PluginResource lang;

    @Override
    public void onEnable() {
        // let the core know the plugin is available
        UntitledLib.setPlugin(this);

        lang = new PluginResource(this, "lang.yml");

        // register the player map
        UntitledLib.register(new OnlinePlayerMap.PlayerMapModule());
        UntitledLib.register(new ScoreboardAPI());

        getCommand("markdown").setExecutor(new MarkdownTest());

        ConfigManager manager = new ConfigManager();
        PluginCommand configCommand = getCommand("config");

        configCommand.setExecutor(manager);
        configCommand.setTabCompleter(manager);

        // send a demo message
        demoLog();
    }

    private void demoLog() {
        getLogger().info(lang.fmt("hello")
                        .constant("plugin", this.getName())
                        .format()
                        .toString());
    }

    @Override
    public void onDisable() {
        UntitledLib.onPluginDisable();
    }
}
