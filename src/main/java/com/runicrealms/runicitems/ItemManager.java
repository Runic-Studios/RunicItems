package com.runicrealms.runicitems;

import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class ItemManager implements Listener {

    private static Map<Integer, RunicItem> cachedItems = new HashMap<Integer, RunicItem>();
    private static Map<Player, Map<Integer, RunicItem>> cachedPlayerItems = new HashMap<Player, Map<Integer, RunicItem>>();

    @EventHandler
    public void onCharacterJoin(CharacterLoadEvent event) {
        if (Plugin.isDatabaseLoadingEnabled()) {
            loadPlayerItems(event.getPlayerCache().getMongoData().getSection("character." + event.getSlot() + ".inventory"), event.getPlayer());
        }
    }

    @EventHandler
    public void onCharacterQuit(CharacterQuitEvent event) {
        if (Plugin.isDatabaseLoadingEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(Plugin.getInstance(), () -> {
                // TODO - save player inventory
                for (Integer key : cachedPlayerItems.get(event.getPlayer()).keySet()) {
                    cachedItems.remove(key);
                }
                cachedPlayerItems.remove(event.getPlayer());
            });
        }
    }

    public static void loadPlayerItems(Data data, Player player) { // Player, Bank
        Map<Integer, RunicItem> playerItems = new HashMap<Integer, RunicItem>();
        for (String key : data.getKeys()) {
            playerItems.put(Integer.parseInt(key), ItemLoader.loadItem(data.getSection(key)));
        }
        cachedPlayerItems.put(player, playerItems);
    }

    public static void loadGlobalItems(Data data) { // Guild Bank, Trade Market
        for (String key : data.getKeys()) {
            cachedItems.put(Integer.parseInt(key), ItemLoader.loadItem(data.getSection(key)));
        }
    }

    public RunicItem getItemFromId(Integer id) {
        return cachedItems.get(id);
    }

}