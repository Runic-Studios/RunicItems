package com.runicrealms.runicitems;

import com.runicrealms.runicitems.config.ConfigUtil;
import com.runicrealms.runicitems.config.AbilityLoader;
import com.runicrealms.runicitems.config.TemplateLoader;
import com.runicrealms.runicitems.exception.InvalidTemplateException;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

    private static Plugin instance;

    @Override
    public void onEnable() {
        instance = this;
        ConfigUtil.initDirs();
        AbilityLoader.loadAbilities();
        try {
            TemplateLoader.loadTemplates();
        } catch (InvalidTemplateException exception) {
            exception.printStackTrace();
        }
    }

    public static Plugin getInstance() {
        return instance;
    }

}
