package com.runicrealms.runicitems;

import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.database.Data;
import com.runicrealms.plugin.database.event.CacheSaveEvent;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.*;
import com.runicrealms.runicitems.item.template.*;
import com.runicrealms.runicitems.item.util.ItemNbtUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

public class ItemManager implements Listener {

    private static long nextId = 0;

    private static File dataFile;
    private static FileConfiguration dataFileConfig;

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
            Data data = event.getPlayerCache().getMongoData().getSection("character." + event.getSlot() + ".inventory");
            for (String key : data.getKeys()) {
                event.getPlayer().getInventory().setItem(Integer.parseInt(key), ItemLoader.loadItem(data.getSection(key), getNextItemId()).generateItem());
            }
        }
    }

    @EventHandler
    public void onCacheSave(CacheSaveEvent event) {
        if (RunicItems.isDatabaseLoadingEnabled()) {
            ItemStack[] contents = event.getPlayer().getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                RunicItem runicItem = getRunicItemFromItemStack(contents[i]);
                if (runicItem != null) {
                    runicItem.addToData(event.getMongoDataSection().getSection("inventory." + i));
                }
            }
        }
    }

    public static RunicItem getRunicItemFromItemStack(ItemStack itemStack) {
        if (!ItemNbtUtils.hasNbtString(itemStack, "template-id")) return null;
        RunicItemTemplate template = TemplateManager.getTemplateFromId(ItemNbtUtils.getNbtString(itemStack, "template-id"));
        if (template == null) return null;
        if (template instanceof RunicItemArmorTemplate) {
            return RunicItemArmor.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemArtifactTemplate) {
            return RunicItemArtifact.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemBookTemplate) {
            return RunicItemBook.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemGenericTemplate) {
            return RunicItemGeneric.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemOffhandTemplate) {
            return RunicItemOffhand.getFromItemStack(itemStack);
        } else if (template instanceof RunicItemWeaponTemplate) {
            return RunicItemWeapon.getFromItemStack(itemStack);
        }
        return null;
    }

}