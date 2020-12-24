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

    private static Map<Long, RunicItem> cachedItems = new HashMap<Integer, RunicItem>();

    @EventHandler
    public void onCharacterJoin(CharacterLoadEvent event) {
        if (Plugin.isDatabaseLoadingEnabled()) {
            loadItems(event.getPlayerCache().getMongoData().getSection("character." + event.getSlot() + ".inventory"));
        }
    }

    @EventHandler
    public void onCharacterQuit(CharacterQuitEvent event) {
        if (Plugin.isDatabaseLoadingEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
                for (ItemStack itemStack : event.getPlayer().getInventory()) {
                    event.getPlayerCache().getMongoData().
                }
            });
        }
    }

    public static void loadItems(Data data) { // Guild Bank, Trade Market
        for (String key : data.getKeys()) {
            cachedItems.put(Long.parseLong(key), ItemLoader.loadItem(data.getSection(key)));
        }
    }

    public RunicItem getItemFromId(Integer id) {
        return cachedItems.get(id);
    }

    public RunicItem getItemFromItemStack(ItemStack itemStack) {
        if (ItemNbtUtils.hasNbtLong(itemStack, "runic-item-id")) {
            return cachedItems.get(ItemNbtUtils.getNbtLong(itemStack, "runic-item-id"));
        }
        return null;
    }

}