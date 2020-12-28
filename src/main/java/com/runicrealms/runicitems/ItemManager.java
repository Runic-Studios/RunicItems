package com.runicrealms.runicitems;

import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.util.ItemNbtUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemManager implements Listener {

    private static Map<Long, RunicItem> cachedItems = new HashMap<Long, RunicItem>();

    @EventHandler
    public void onCharacterJoin(CharacterLoadEvent event) {
        if (Plugin.isDatabaseLoadingEnabled()) {
            loadItems(event.getPlayerCache().getMongoData().getSection("character." + event.getSlot() + ".inventory"));
        }
    }

    // TODO - switch to new runic core event
    @EventHandler
    public void onCharacterQuit(CharacterQuitEvent event) {
        if (Plugin.isDatabaseLoadingEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
                ItemStack[] contents = event.getPlayer().getInventory().getContents();
                for (int i = 0; i < contents.length; i++) {
                    RunicItem runicItem = getItemFromItemStack(contents[i]);
                    if (runicItem != null) {
                        runicItem.addToData(event.getPlayerCache().getMongoData().getSection("character." + event.getSlot() + ".inventory." + i));
                    }
                }
            });
        }
    }

    public static void loadItems(Data data) { // Guild Bank, Trade Market
        for (String key : data.getKeys()) {
            RunicItem loadedItem = ItemLoader.loadItem(data.getSection(key)); // TODO - load id, itemOwner
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