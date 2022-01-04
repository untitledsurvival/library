package com.untitledsurvival.lib.plugin;

import com.untitledsurvival.lib.lang.LangFile;
import com.untitledsurvival.lib.lang.placeholder.PlaceholderAPI;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class UntitledLibPlugin extends JavaPlugin {
    @Getter private static LangFile lang;

    @Override
    public void onEnable() {
        lang = new LangFile(this, "lang.yml");

        // send a demo message
        PlaceholderAPI.getEventStack().push("plugin", getName());
        getLogger().info(lang.pFormat("hello"));
    }
}
