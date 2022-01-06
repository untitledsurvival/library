package com.untitledsurvival.lib.plugin;

import com.untitledsurvival.lib.UntitledLib;
import com.untitledsurvival.lib.lang.LangFile;
import com.untitledsurvival.lib.lang.placeholder.PlaceholderAPI;
import com.untitledsurvival.lib.plugin.commands.MarkdownTest;
import com.untitledsurvival.lib.plugin.utils.OnlinePlayerMap;
import com.untitledsurvival.lib.scoreboard.ScoreboardAPI;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class UntitledLibPlugin extends JavaPlugin {
    @Getter private static LangFile lang;

    @Override
    public void onEnable() {
        // let the core know the plugin is available
        UntitledLib.setPlugin(this);

        lang = new LangFile(this, "lang.yml");

        // register the player map
        UntitledLib.register(new OnlinePlayerMap.PlayerMapModule());
        UntitledLib.register(new ScoreboardAPI());

        getCommand("markdown").setExecutor(new MarkdownTest());

        // send a demo message
        demoLog();
    }

    private void demoLog() {
        PlaceholderAPI.getEventStack().push("plugin", getName());
        getLogger().info(lang.pFormat("hello"));
    }

    @Override
    public void onDisable() {
        UntitledLib.onPluginDisable();
    }
}
