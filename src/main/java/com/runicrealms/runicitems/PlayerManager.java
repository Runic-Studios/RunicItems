package com.runicrealms.runicitems;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.events.OffhandEquipEvent;
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

    @EventHandler(priority = EventPriority.NORMAL)
    // Fire before other character load events so checking stats functions properly
    public void onCharacterLoad(CharacterLoadEvent e) {
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
                case HELMET: cachedPlayerStats.get(uuid).updateHelmet();
                case CHESTPLATE: cachedPlayerStats.get(uuid).updateChestplate();
                case LEGGINGS: cachedPlayerStats.get(uuid).updateLeggings();
                case BOOTS: cachedPlayerStats.get(uuid).updateBoots();
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (!cachedPlayerStats.containsKey(event.getPlayer().getUniqueId())) return;
        if (event.isCancelled()) return;
        Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> cachedPlayerStats.get(event.getPlayer().getUniqueId()).updateWeapon());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onOffhandEquip(OffhandEquipEvent event) {
        if (!event.isCancelled()) {
            Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> cachedPlayerStats.get(event.getPlayer().getUniqueId()).updateOffhand());
        }
    }

    public static Map<UUID, PlayerStatHolder> getCachedPlayerStats() {
        return cachedPlayerStats;
    }

}