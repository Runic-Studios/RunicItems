package com.runicrealms.runicitems;

import com.runicrealms.runicitems.config.ConfigUtil;
import com.runicrealms.runicitems.config.SpellLoader;
import com.runicrealms.runicitems.config.TemplateLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

    private static Plugin instance;

    @Override
    public void onEnable() {
        instance = this;
        ConfigUtil.initDirs();
        SpellLoader.loadSpells();
        TemplateLoader.loadTemplates();
    }

    public static Plugin getInstance() {
        return instance;
    }

}
