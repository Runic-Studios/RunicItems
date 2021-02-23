package com.runicrealms.runicitems;

import com.runicrealms.runicitems.item.util.ItemNbtUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DupeManager implements Listener {

    public static final int MAX_ITEMS_CLICKED_CACHE_LENGTH = 50;

    private static final Map<Player, ConcurrentLinkedQueue<ItemStack>> itemsClicked = new ConcurrentHashMap<>();

    private static long nextId = Long.MIN_VALUE;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            final Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() != null) {
                Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), () -> {
                    for (ItemStack itemOne : itemsClicked.get(player)) {
                        if (checkItemsDuped(itemOne, event.getCurrentItem())) {
                            player.getInventory().remove(event.getCurrentItem());
                            // TODO JDA
                        }
                    }
                    if (!itemsClicked.get(player).contains(event.getCurrentItem())) {
                        while (itemsClicked.get(player).size() >= MAX_ITEMS_CLICKED_CACHE_LENGTH) {
                            itemsClicked.get(player).remove();
                        }
                        itemsClicked.get(player).add(event.getCurrentItem());
                    }
                });
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        itemsClicked.put(event.getPlayer(), new ConcurrentLinkedQueue<>());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        itemsClicked.remove(event.getPlayer());
    }

    public static boolean checkItemsDuped(ItemStack itemOne, ItemStack itemTwo) {
        if (!ItemNbtUtils.hasNbtLong(itemOne, "id")) {
            if (ItemNbtUtils.hasNbtString(itemOne, "template-id")) {
                ItemNbtUtils.setNbt(itemOne, "id", getNextItemId());
            }
            return false;
        }
        if (!ItemNbtUtils.hasNbtInteger(itemOne, "count")) return false;
        if (ItemNbtUtils.getNbtInteger(itemOne, "count") != itemOne.getAmount()) {
            ItemNbtUtils.setNbt(itemOne, "count", itemOne.getAmount());
            return false;
        }
        if (!ItemNbtUtils.hasNbtLong(itemTwo, "id")) {
            if (ItemNbtUtils.hasNbtString(itemTwo, "template-id")) {
                ItemNbtUtils.setNbt(itemTwo, "id", getNextItemId());
            }
            return false;
        }
        if (!ItemNbtUtils.hasNbtInteger(itemTwo, "count")) return false;
        if (ItemNbtUtils.getNbtInteger(itemTwo, "count") != itemTwo.getAmount()) {
            ItemNbtUtils.setNbt(itemTwo, "count", itemTwo.getAmount());
            return false;
        }
        if (ItemNbtUtils.getNbtLong(itemOne, "id") == ItemNbtUtils.getNbtLong(itemTwo, "id")) {
            return true;
        }
        return false;
    }
    public static long getNextItemId() {
        return nextId++;
    }

}
