package com.runicrealms.runicitems.listeners;

import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicguilds.Plugin;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.RunicItemBag;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class GoldPouchListener implements Listener {

    private static final int GOLD_POUCH_INTERACT_DELAY = 10; // ticks
    private final HashSet<UUID> playersUpdatingPouches = new HashSet<>(); // prevents exploits

    @EventHandler
    public void onGoldPouchClick(PlayerInteractEvent e) {
        if (playersUpdatingPouches.contains(e.getPlayer().getUniqueId())) return;
        Player player = e.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getItem() == null) return;
        if (RunicItemsAPI.getRunicItemFromItemStack(e.getItem()) == null) return;
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(e.getItem());
        if (!(runicItem instanceof RunicItemBag)) return;
        RunicItemBag goldPouch = (RunicItemBag) runicItem;
        if (player.getInventory().firstEmpty() == -1) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You must have empty inventory space to modify your gold pouch!");
            return;
        }
        playersUpdatingPouches.add(player.getUniqueId());
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            goldPouch.fillPouch(player);
            ItemStack filledPouch = goldPouch.generateItem();
            ItemRemover.takeItem(player, e.getItem(), 1);
            player.getInventory().addItem(filledPouch);
        } else if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            player.playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 0.5f, 1.0f);
            int currentCoins = goldPouch.emptyPouch();
            ItemStack emptyPouch = goldPouch.generateItem();
            ItemRemover.takeItem(player, e.getItem(), 1);
            player.getInventory().addItem(emptyPouch);
            // give coins contained inside, drops remaining coins on the floor
            HashMap<Integer, ItemStack> coinsToAdd = player.getInventory().addItem(CurrencyUtil.goldCoin(currentCoins));
            for (ItemStack is : coinsToAdd.values()) {
                player.getWorld().dropItem(player.getLocation(), is);
            }
        }
        Bukkit.getScheduler().runTaskLater(Plugin.getInstance(), () -> playersUpdatingPouches.remove(player.getUniqueId()), GOLD_POUCH_INTERACT_DELAY);
    }

}
