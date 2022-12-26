package com.runicrealms.runicitems.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicitems.RunicItems;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItemDynamic;
import com.runicrealms.runicitems.item.event.RunicItemGenericTriggerEvent;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.UUID;

public class GoldPouchListener implements Listener {

    private static final int GOLD_POUCH_INTERACT_DELAY = 10; // ticks
    private static final String POUCH_ID = "gold-pouch";
    private final HashSet<UUID> playersUpdatingPouches = new HashSet<>(); // prevents exploits

    /**
     * Empties our RunicItemDynamic object of its stored coins with a reference to the previous amount
     * Should be used before generateItem
     *
     * @return the previously held coins amount
     */
    public int emptyPouch(RunicItemDynamic runicItemDynamic) {
        int coins = runicItemDynamic.getDynamicField();
        runicItemDynamic.setDynamicField(0);
        return coins;
    }

    /**
     * Fills our gold pouch item using gold from the player's inventory
     */
    public void fillPouch(Player player, RunicItemDynamic runicItemDynamic) {
        int currentAmount = runicItemDynamic.getDynamicField();
        int maxAmount = Integer.parseInt(runicItemDynamic.getData().get("maxCoins"));
        int amountToFill = maxAmount - currentAmount;
        // if player has enough coins to fill the pouch, fill it
        if (RunicCore.getShopAPI().hasItem(player, CurrencyUtil.goldCoin(), amountToFill)) {
            ItemRemover.takeItem(player, CurrencyUtil.goldCoin(), amountToFill);
            runicItemDynamic.setDynamicField(maxAmount);
            return;
        }
        // if player does not have enough coins to fill it, start filling it using the largest stack size possible
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 64);
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 48);
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 32);
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 16);
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 8);
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 4);
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 2);
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 1);
        runicItemDynamic.setDynamicField(currentAmount);
    }

    @EventHandler
    public void onGoldPouchTrigger(RunicItemGenericTriggerEvent e) {
        if (playersUpdatingPouches.contains(e.getPlayer().getUniqueId())) return;
        if (!e.getItem().getTemplateId().equals(POUCH_ID)) return;
        RunicItemDynamic goldPouch = (RunicItemDynamic) e.getItem();
        Player player = e.getPlayer();
        if (player.getInventory().getItemInOffHand().getType() != Material.AIR
                && RunicItemsAPI.isRunicItemSimilar(player.getInventory().getItemInOffHand(), e.getItemStack()))
            return; // dupe bugfix
        playersUpdatingPouches.add(e.getPlayer().getUniqueId());
        if (e.getTrigger() == ClickTrigger.LEFT_CLICK) {
            player.playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 0.5f, 1.0f);
            int currentCoins = emptyPouch(goldPouch);
            ItemStack emptyPouch = goldPouch.generateItem();
            ItemRemover.takeItem(player, e.getItemStack(), 1);
            // give coins contained inside, drops remaining coins on the floor
            int remaining = currentCoins;
            while (remaining != 0) {
                if (remaining >= 64) {
                    RunicItemsAPI.addItem(player.getInventory(), CurrencyUtil.goldCoin(64), player.getLocation());
                    remaining -= 64;
                } else {
                    RunicItemsAPI.addItem(player.getInventory(), CurrencyUtil.goldCoin(remaining), player.getLocation());
                    remaining = 0;
                }
            }
            RunicItemsAPI.addItem(player.getInventory(), emptyPouch, player.getLocation());
        } else if (e.getTrigger() == ClickTrigger.RIGHT_CLICK) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
            fillPouch(player, goldPouch);
            ItemStack filledPouch = goldPouch.generateItem();
            ItemRemover.takeItem(player, e.getItemStack(), 1);
            RunicItemsAPI.addItem(player.getInventory(), filledPouch);
        }
        Bukkit.getScheduler().runTaskLater(RunicItems.getInstance(), () -> playersUpdatingPouches.remove(player.getUniqueId()), GOLD_POUCH_INTERACT_DELAY);
    }

    /**
     * If a player doesn't have enough coins to fill a pouch, we manually start filling it by the largest stack possible
     *
     * @param currentAmount the amount of coins in the pouch
     * @param maxAmount     the total amount of coins the pouch can hold
     * @param player        to check inventory from
     * @param stackSize     the amount of coins we will try to fill
     * @return the new current amount of coins in the pouch
     */
    private int removeGoldStackSize(int currentAmount, int maxAmount, Player player, int stackSize) {
        while (RunicCore.getShopAPI().hasItem(player, CurrencyUtil.goldCoin(), stackSize) && currentAmount < maxAmount) {
            // remove it, add to pouch
            ItemRemover.takeItem(player, CurrencyUtil.goldCoin(), stackSize);
            currentAmount += stackSize;
        }
        return currentAmount;
    }
}
