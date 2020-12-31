package com.runicrealms.runicitems;

import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.database.Data;
import com.runicrealms.plugin.database.event.CacheSaveEvent;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.util.ItemNbtUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ItemManager implements Listener {

    public static final int CACHED_IDS = 1024;
    public static final int MIN_CACHED_IDS = 64;

    private static long nextId;
    private static long lastId;

    private static File dataFile;
    private static FileConfiguration dataFileConfig;

    private static Map<Long, RunicItem> cachedItems = new HashMap<Long, RunicItem>();

    public static void initializeDataFile() {
        dataFile = new File(Plugin.getInstance().getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        dataFileConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (!dataFileConfig.contains("next-id")) {
            dataFileConfig.set("next-id", Long.MIN_VALUE);
        }
        nextId = dataFileConfig.getLong("next-id");
        lastId = nextId + CACHED_IDS;
        dataFileConfig.set("next-id", lastId);
        Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), ItemManager::saveDataFile);
    }

    private static void saveDataFile() { // Should not be called sync!
        try {
            dataFileConfig.save(dataFile);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static long getNextItemId() {
        long nextItemId = nextId++;
        if (lastId - MIN_CACHED_IDS <= nextItemId) {
            lastId += CACHED_IDS;
            dataFileConfig.set("next-id", lastId);
            Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), ItemManager::saveDataFile);
        }
        return nextItemId;
    }

    @EventHandler
    public void onCharacterJoin(CharacterLoadEvent event) {
        if (Plugin.isDatabaseLoadingEnabled()) {
            loadItems(event.getPlayerCache().getMongoData().getSection("character." + event.getSlot() + ".inventory"));
        }
    }

    @EventHandler
    public void onCacheSave(CacheSaveEvent event) {
        if (Plugin.isDatabaseLoadingEnabled()) {
            ItemStack[] contents = event.getPlayer().getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                RunicItem runicItem = getItemFromItemStack(contents[i]);
                if (runicItem != null) {
                    runicItem.addToData(event.getMongoDataSection().getSection("inventory." + i));
                }
            }
        }
    }

    public static void loadItems(Data data) {
        for (String key : data.getKeys()) {
            RunicItem loadedItem = ItemLoader.loadItem(data.getSection(key));
            cachedItems.put(loadedItem.getId(), loadedItem);
        }
    }

    public static RunicItem getItemFromId(Long id) {
        return cachedItems.get(id);
    }

    public static RunicItem getItemFromItemStack(ItemStack itemStack) {
        if (ItemNbtUtils.hasNbtLong(itemStack, "runic-item-id")) {
            return cachedItems.get(ItemNbtUtils.getNbtLong(itemStack, "runic-item-id"));
        }
        return null;
    }

}