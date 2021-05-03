package com.runicrealms.runicitems;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.armorequip.ArmorType;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
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

    @EventHandler(priority = EventPriority.LOWEST)
    // Fire before other character load events so checking stats functions properly
    public void onCharacterLoad(CharacterLoadEvent e) {
        cachedPlayerStats.put(e.getPlayer().getUniqueId(), new PlayerStatHolder(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    // Fire after other character load events so checking stats functions properly
    public void onPlayerQuit(PlayerQuitEvent event) {
        cachedPlayerStats.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(ArmorEquipEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!cachedPlayerStats.containsKey(uuid)) return;
        if (e.isCancelled()) return;
        ArmorType armorType = ArmorType.matchType(e.getNewArmorPiece());
        if (armorType == ArmorType.HELMET) {
            Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> cachedPlayerStats.get(uuid).updateHelmet());
        } else if (armorType == ArmorType.CHESTPLATE) {
            Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> cachedPlayerStats.get(uuid).updateChestplate());
        } else if (armorType == ArmorType.LEGGINGS) {
            Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> cachedPlayerStats.get(uuid).updateLeggings());
        } else if (armorType == ArmorType.BOOTS) {
            Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> cachedPlayerStats.get(uuid).updateBoots());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (!cachedPlayerStats.containsKey(event.getPlayer().getUniqueId())) return;
        if (!event.isCancelled()) {
            cachedPlayerStats.get(event.getPlayer().getUniqueId()).updateWeapon();
        }
    }

    public static Map<UUID, PlayerStatHolder> getCachedPlayerStats() {
        return cachedPlayerStats;
    }

}