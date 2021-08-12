package com.runicrealms.runicitems.listeners;

import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Prevents dropping of soulbound items
 */
public class SoulboundListener implements Listener {

    @EventHandler
    public void onSoulboundItemDrop(PlayerDropItemEvent e) {
        if (RunicItemsAPI.getRunicItemFromItemStack(e.getItemDrop().getItemStack()) == null) return;
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(e.getItemDrop().getItemStack());
        boolean isSoulbound = runicItem.getTags().contains(RunicItemTag.SOULBOUND);
        if (isSoulbound && e.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            e.getPlayer().sendMessage(ChatColor.GRAY + "This item is soulbound.");
        }
    }
}
