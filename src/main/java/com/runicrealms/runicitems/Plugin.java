package com.runicrealms.runicitems;

import co.aikar.commands.PaperCommandManager;
import com.runicrealms.runicitems.command.RunicItemCommand;
import com.runicrealms.runicitems.config.ConfigUtil;
import com.runicrealms.runicitems.config.AbilityLoader;
import com.runicrealms.runicitems.config.TemplateLoader;
import com.runicrealms.runicitems.exception.InvalidTemplateException;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

    private static Plugin instance;

    private static PaperCommandManager commandManager;

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
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new RunicItemCommand());
    }

    public static Plugin getInstance() {
        return instance;
    }

    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

}
