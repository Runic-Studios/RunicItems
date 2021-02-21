package com.runicrealms.runicitems;

import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.database.Data;
import com.runicrealms.plugin.database.event.CacheSaveEvent;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.util.ItemNbtUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ItemManager implements Listener {

    private static long nextId = 0;

    private static File dataFile;
    private static FileConfiguration dataFileConfig;

    private static final Map<Long, RunicItem> cachedItems = new HashMap<Long, RunicItem>();

    public static void initializeDataFile() {
        dataFile = new File(RunicItems.getInstance().getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        // Nothing here now, might delete idk
        Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), ItemManager::saveDataFile);
    }

    private static void saveDataFile() { // Should not be called sync!
        try {
            dataFileConfig.save(dataFile);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static long getNextItemId() {
        return nextId++;
    }

    @EventHandler
    public void onCharacterJoin(CharacterLoadEvent event) {
        if (RunicItems.isDatabaseLoadingEnabled()) {
            loadItems(event.getPlayerCache().getMongoData().getSection("character." + event.getSlot() + ".inventory"));
        }
    }

    @EventHandler
    public void onCacheSave(CacheSaveEvent event) {
        if (RunicItems.isDatabaseLoadingEnabled()) {
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
            RunicItem loadedItem = ItemLoader.loadItem(data.getSection(key), ItemManager.getNextItemId());
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