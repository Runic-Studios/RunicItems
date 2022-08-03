package com.runicrealms.runicitems;

import com.runicrealms.plugin.ArmorType;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.events.ArmorEquipEvent;
import com.runicrealms.runicitems.player.PlayerStatHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager implements Listener {

    private static final Map<UUID, PlayerStatHolder> cachedPlayerStats = new HashMap<>();

    /**
     * Fire before character loaded events so checking stats functions properly
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCharacterLoad(CharacterSelectEvent e) {
        cachedPlayerStats.put(e.getPlayer().getUniqueId(), new PlayerStatHolder(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    // Fire after other character load events so checking stats functions properly
    public void onPlayerQuit(PlayerQuitEvent event) {
        cachedPlayerStats.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    // Fire before other armor equip events so checking stats functions properly
    public void onArmorEquipEvent(ArmorEquipEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!cachedPlayerStats.containsKey(uuid)) return;
        if (event.isCancelled()) return;
        Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> {
            switch (event.getType()) {
                case HELMET:
                    cachedPlayerStats.get(uuid).updateItems(ArmorType.HELMET);
                case CHESTPLATE:
                    cachedPlayerStats.get(uuid).updateItems(ArmorType.CHESTPLATE);
                case LEGGINGS:
                    cachedPlayerStats.get(uuid).updateItems(ArmorType.LEGGINGS);
                case BOOTS:
                    cachedPlayerStats.get(uuid).updateItems(ArmorType.BOOTS);
                case OFFHAND:
                    cachedPlayerStats.get(uuid).updateOffhand();
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (!cachedPlayerStats.containsKey(event.getPlayer().getUniqueId())) return;
        if (event.isCancelled()) return;
        Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> cachedPlayerStats.get(event.getPlayer().getUniqueId()).updateWeapon());
    }

    public static Map<UUID, PlayerStatHolder> getCachedPlayerStats() {
        return cachedPlayerStats;
    }

}