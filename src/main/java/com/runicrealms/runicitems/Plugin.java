package com.runicrealms.runicitems;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import com.runicrealms.runicitems.command.RunicItemCommand;
import com.runicrealms.runicitems.config.ConfigUtil;
import com.runicrealms.runicitems.config.AbilityLoader;
import com.runicrealms.runicitems.config.TemplateLoader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin {

    private static Plugin instance;

    private static PaperCommandManager commandManager;

    private static boolean databaseLoadingEnabled = true;

    @Override
    public void onEnable() {
        // Setup base
        instance = this;
        ConfigUtil.initDirs();

        // Load YML files
        AbilityLoader.loadAbilities();
        try {
            TemplateLoader.loadTemplates();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(new ItemManager(), this);

        // Register Commands
        commandManager = new PaperCommandManager(this);
        commandManager.getCommandConditions().addCondition("is-player", context -> {
            if (!(context.getIssuer().getIssuer() instanceof Player)) throw new ConditionFailedException("This command cannot be run from console!");
        });
        commandManager.getCommandConditions().addCondition("is-op", context -> {
            if (!context.getIssuer().getIssuer().isOp()) throw new ConditionFailedException("You must be an operator to run this command!");
        });
        commandManager.registerCommand(new RunicItemCommand());
    }

    public static Plugin getInstance() {
        return instance;
    }

    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public static void setDatabaseLoadingEnabled(boolean enabled) {
        databaseLoadingEnabled = enabled;
    }

    public static boolean isDatabaseLoadingEnabled() {
        return databaseLoadingEnabled;
    }

}
