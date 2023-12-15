package com.runicrealms.plugin.runicitems;

import com.runicrealms.plugin.common.event.ArmorEquipEvent;
import com.runicrealms.plugin.rdb.event.CharacterHasQuitEvent;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.plugin.runicitems.item.event.RunicStatUpdateEvent;
import com.runicrealms.plugin.runicitems.player.PlayerEquipmentCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager implements Listener {
    private static final Map<UUID, PlayerEquipmentCache> cachedPlayerStats = new ConcurrentHashMap<>();

    public static Map<UUID, PlayerEquipmentCache> getCachedPlayerStats() {
        return cachedPlayerStats;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    // Fire after other armor equip events to allow them to cancel it
    public void onArmorEquipEvent(ArmorEquipEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!cachedPlayerStats.containsKey(uuid)) return;
        if (event.isCancelled()) return;
        Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> {
            PlayerEquipmentCache holder = cachedPlayerStats.get(uuid);
            switch (event.getType()) {
                case HELMET -> holder.updateItems(false, PlayerEquipmentCache.StatHolderType.HELMET);
                case CHESTPLATE -> holder.updateItems(false, PlayerEquipmentCache.StatHolderType.CHESTPLATE);
                case LEGGINGS -> holder.updateItems(false, PlayerEquipmentCache.StatHolderType.LEGGINGS);
                case BOOTS -> holder.updateItems(false, PlayerEquipmentCache.StatHolderType.BOOTS);
                case OFFHAND -> holder.updateItems(false, PlayerEquipmentCache.StatHolderType.OFFHAND);
            }
            RunicStatUpdateEvent statUpdateEvent = new RunicStatUpdateEvent(event.getPlayer(), holder);
            Bukkit.getPluginManager().callEvent(statUpdateEvent);
        });
    }

    /**
     * Fire before character loaded events so checking stats functions properly
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onCharacterLoad(CharacterLoadedEvent event) {
        cachedPlayerStats.put(event.getPlayer().getUniqueId(), new PlayerEquipmentCache(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (!cachedPlayerStats.containsKey(event.getPlayer().getUniqueId())) return;
        if (event.isCancelled()) return;
        if (event.getNewSlot() == event.getPreviousSlot()) return;
        Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> cachedPlayerStats.get(event.getPlayer().getUniqueId()).updateItems(false, PlayerEquipmentCache.StatHolderType.WEAPON));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    // Fire after other character load events so checking stats functions properly
    public void onPlayerQuit(CharacterHasQuitEvent event) {
        cachedPlayerStats.remove(event.getPlayer().getUniqueId());
    }

}