package com.runicrealms.runicitems;

import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.runicitems.player.PlayerStatHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager implements Listener {

    private static final Map<Player, PlayerStatHolder> cachedPlayerStats = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST) // Fire before other character load events so checking stats functions properly
    public void onCharacterLoad(CharacterLoadEvent event) {
        cachedPlayerStats.put(event.getPlayer(), new PlayerStatHolder(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST) // Fire after other character load events so checking stats functions properly
    public void onPlayerQuit(PlayerQuitEvent event) {
        cachedPlayerStats.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (!cachedPlayerStats.containsKey(player)) return;
            if (!event.isCancelled()) {
                if (event.getInventory().getType() == InventoryType.PLAYER) {
                    if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                        if (event.getSlot() == 39) { // Helmet
                            Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> cachedPlayerStats.get(player).updateHelmet());
                        } else if (event.getSlot() == 38) { // Chestplate
                            Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> cachedPlayerStats.get(player).updateChestplate());
                        } else if (event.getSlot() == 37) { // Leggings
                            Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> cachedPlayerStats.get(player).updateLeggings());
                        } else if (event.getSlot() == 36) { // Boots
                            Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> cachedPlayerStats.get(player).updateBoots());
                        } else if (event.getSlot() == player.getInventory().getHeldItemSlot()) { // Weapon
                            Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> cachedPlayerStats.get(player).updateWeapon());
                        } else if (event.getSlot() == 40) { // Offhand
                            Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> cachedPlayerStats.get(player).updateOffhand());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (!cachedPlayerStats.containsKey(event.getPlayer())) return;
        if (!event.isCancelled()) {
            cachedPlayerStats.get(event.getPlayer()).updateWeapon();
        }
    }

    public static Map<Player, PlayerStatHolder> getCachedPlayerStats() {
        return cachedPlayerStats;
    }

}