package com.runicrealms.runicitems;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.runicrealms.plugin.rdb.event.DatabaseInitializeEvent;
import com.runicrealms.runicitems.api.DataAPI;
import com.runicrealms.runicitems.api.InventoryAPI;
import com.runicrealms.runicitems.api.WeaponSkinAPI;
import com.runicrealms.runicitems.command.RunicItemCommand;
import com.runicrealms.runicitems.command.WeaponSkinCommand;
import com.runicrealms.runicitems.command.WeaponryCommand;
import com.runicrealms.runicitems.config.AbilityLoader;
import com.runicrealms.runicitems.config.ConfigUtil;
import com.runicrealms.runicitems.config.TemplateLoader;
import com.runicrealms.runicitems.converter.RunicItemReadConverter;
import com.runicrealms.runicitems.converter.RunicItemWriteConverter;
import com.runicrealms.runicitems.listeners.GoldPouchListener;
import com.runicrealms.runicitems.listeners.ItemSpawnListener;
import com.runicrealms.runicitems.listeners.MoveToInventoryListener;
import com.runicrealms.runicitems.listeners.PlayerMTIListener;
import com.runicrealms.runicitems.listeners.SoulboundListener;
import com.runicrealms.runicitems.model.InventoryDataManager;
import com.runicrealms.runicitems.model.MongoTask;
import com.runicrealms.runicitems.weaponskin.WeaponSkinManager;
import com.runicrealms.runicitems.weaponskin.ui.WeaponAppearancesUIListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class RunicItems extends JavaPlugin implements Listener {
    private static RunicItems instance;
    private static PaperCommandManager commandManager;
    private static DataAPI dataAPI;
    private static WeaponSkinAPI weaponSkinAPI;
    private static MongoTask mongoTask;
    //    private static JDA jda;
    private static InventoryAPI INVENTORY_API;

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

    public static InventoryAPI getInventoryAPI() {
        return INVENTORY_API;
    }

    public static WeaponSkinAPI getWeaponSkinAPI() {
        return weaponSkinAPI;
    }

//    public static JDA getJda() {
//        return jda;
//    }

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
        INVENTORY_API = new ItemManager();
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
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new PlayerMTIListener(), this);
        Bukkit.getPluginManager().registerEvents(new MoveToInventoryListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemSpawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new DupeManager(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerManager(), this);
        Bukkit.getPluginManager().registerEvents(new GemManager(), this);
        Bukkit.getPluginManager().registerEvents(new SoulboundListener(), this);
        Bukkit.getPluginManager().registerEvents(new GoldPouchListener(), this);
        Bukkit.getPluginManager().registerEvents(new WeaponAppearancesUIListener(), this);

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
        commandManager.registerCommand(new WeaponSkinCommand());
        commandManager.registerCommand(new WeaponryCommand());

        // Start JDA
//        try {
//            JDABuilder builder = JDABuilder.createDefault("ODEzNTc2Nzg4MjI2ODY3MjIx.YDRUbQ.qm2ri55Jj62J0R06MBElc_rm_1M");
//            builder.setStatus(OnlineStatus.ONLINE);
//            builder.setActivity(Activity.watching("over Runic Realms"));
//            builder.addEventListeners(new RunicItemsJDAListener());
//            jda = builder.build();
//        } catch (Exception exception) {
//            exception.printStackTrace();
//        }
    }

    @EventHandler
    public void onDatabaseLoad(DatabaseInitializeEvent event) {
        dataAPI = new InventoryDataManager();
        mongoTask = new MongoTask();
        new RunicItemReadConverter();
        new RunicItemWriteConverter();
        weaponSkinAPI = new WeaponSkinManager();
    }

}
