package com.runicrealms.runicitems;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import com.runicrealms.runicitems.command.RunicItemCommand;
import com.runicrealms.runicitems.config.ConfigUtil;
import com.runicrealms.runicitems.config.AbilityLoader;
import com.runicrealms.runicitems.config.TemplateLoader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RunicItems extends JavaPlugin {

    private static RunicItems instance;

    private static PaperCommandManager commandManager;
    private static JDA jda;

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
        Bukkit.getPluginManager().registerEvents(new DupeManager(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerManager(), this);

        // Register Commands
        commandManager = new PaperCommandManager(this);
        commandManager.getCommandConditions().addCondition("is-player", context -> {
            if (!(context.getIssuer().getIssuer() instanceof Player)) throw new ConditionFailedException("This command cannot be run from console!");
        });
        commandManager.getCommandConditions().addCondition("is-op", context -> {
            if (!context.getIssuer().getIssuer().isOp()) throw new ConditionFailedException("You must be an operator to run this command!");
        });
        commandManager.registerCommand(new RunicItemCommand());

        // Start JDA
        try {
            JDABuilder builder = JDABuilder.createDefault("ODEzNTc2Nzg4MjI2ODY3MjIx.YDRUbQ.qm2ri55Jj62J0R06MBElc_rm_1M");
            builder.setStatus(OnlineStatus.ONLINE);
            builder.setActivity(Activity.watching("over Runic Realms"));
            jda = builder.build();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public static RunicItems getInstance() {
        return instance;
    }

    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public static JDA getJda() {
        return jda;
    }

    public static void setDatabaseLoadingEnabled(boolean enabled) {
        databaseLoadingEnabled = enabled;
    }

    public static boolean isDatabaseLoadingEnabled() {
        return databaseLoadingEnabled;
    }

}
