package com.runicrealms.runicitems;

import com.runicrealms.libs.acf.ConditionFailedException;
import com.runicrealms.libs.acf.PaperCommandManager;
import com.runicrealms.libs.taskchain.BukkitTaskChainFactory;
import com.runicrealms.libs.taskchain.TaskChain;
import com.runicrealms.libs.taskchain.TaskChainFactory;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.runicitems.api.DataAPI;
import com.runicrealms.runicitems.command.RunicItemCommand;
import com.runicrealms.runicitems.config.AbilityLoader;
import com.runicrealms.runicitems.config.ConfigUtil;
import com.runicrealms.runicitems.config.TemplateLoader;
import com.runicrealms.runicitems.converter.RunicItemReadConverter;
import com.runicrealms.runicitems.converter.RunicItemWriteConverter;
import com.runicrealms.runicitems.listeners.*;
import com.runicrealms.runicitems.model.InventoryDataManager;
import com.runicrealms.runicitems.model.MongoTask;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class RunicItems extends JavaPlugin implements Listener {
    private static RunicItems instance;
    private static PaperCommandManager commandManager;
    private static DataAPI dataAPI;
    private static MongoTask mongoTask;
    private static JDA jda;

    private static boolean databaseLoadingEnabled = true;
    private static TaskChainFactory taskChainFactory;

    public static RunicItems getInstance() {
        return instance;
    }

    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public static DataAPI getDataAPI() {
        return dataAPI;
    }

    public static MongoTask getMongoTask() {
        return mongoTask;
    }

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }

    public static JDA getJda() {
        return jda;
    }

    public static boolean isDatabaseLoadingEnabled() {
        return databaseLoadingEnabled;
    }

    public static void setDatabaseLoadingEnabled(boolean enabled) {
        databaseLoadingEnabled = enabled;
    }

    @Override
    public void onEnable() {
        // Setup base
        instance = this;
        taskChainFactory = BukkitTaskChainFactory.create(this);
        dataAPI = new InventoryDataManager();
        mongoTask = new MongoTask();
        new RunicItemReadConverter();
        new RunicItemWriteConverter();
        ConfigUtil.initDirs();

        // Load YML files
        AbilityLoader.loadAbilities();
        try {
            TemplateLoader.loadTemplates();
            LootManager.sortItems(TemplateManager.getTemplates());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(new ItemManager(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMTIListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemSpawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new DupeManager(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerManager(), this);
        Bukkit.getPluginManager().registerEvents(new GemManager(), this);
        Bukkit.getPluginManager().registerEvents(new SoulboundListener(), this);
        Bukkit.getPluginManager().registerEvents(new GoldPouchListener(), this);
        Bukkit.getPluginManager().registerEvents(new ArtifactOnCastListener(), this);
        Bukkit.getPluginManager().registerEvents(new ArtifactOnHitListener(), this);
        Bukkit.getPluginManager().registerEvents(new ArtifactOnKillListener(), this);

        // Register Commands
        commandManager = new PaperCommandManager(this);
        commandManager.getCommandConditions().addCondition("is-player", context -> {
            if (!(context.getIssuer().getIssuer() instanceof Player))
                throw new ConditionFailedException("This command cannot be run from console!");
        });
        commandManager.getCommandConditions().addCondition("is-op", context -> {
            if (!context.getIssuer().getIssuer().isOp())
                throw new ConditionFailedException("You must be an operator to run this command!");
        });
        commandManager.registerCommand(new RunicItemCommand());

        // Start JDA
        try {
            JDABuilder builder = JDABuilder.createDefault("ODEzNTc2Nzg4MjI2ODY3MjIx.YDRUbQ.qm2ri55Jj62J0R06MBElc_rm_1M");
            builder.setStatus(OnlineStatus.ONLINE);
            builder.setActivity(Activity.watching("over Runic Realms"));
            builder.addEventListeners(new RunicItemsJDAListener());
            jda = builder.build();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

//    @EventHandler
//    public void onPreShutdown(PreshutdownEvent)

    /**
     * Properly shut down JDA during a save
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onMongoSave(MongoSaveEvent event) {
        jda.shutdownNow();
//        jda.shutdown();
        jda = null;
    }

}
