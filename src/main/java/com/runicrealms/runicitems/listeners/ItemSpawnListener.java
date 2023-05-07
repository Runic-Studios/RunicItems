package com.runicrealms.runicitems.listeners;

import com.runicrealms.runicitems.RunicItems;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Used to combine similar ItemStacks on the floor
 */
public class ItemSpawnListener implements Listener {
    private static final double ITEM_MERGE_RADIUS = 1.5; // blocks

    private void findNearbyItemsAndMerge(Item newItem) {
        ItemStack newItemStack = newItem.getItemStack();
        newItem.getNearbyEntities(ITEM_MERGE_RADIUS, ITEM_MERGE_RADIUS, ITEM_MERGE_RADIUS).forEach(entity -> {
            if (entity instanceof Item nearbyItem) {
                ItemStack nearbyItemStack = nearbyItem.getItemStack();

                // Check if items are the same and can be merged
                if (RunicItemsAPI.isRunicItemSimilar(newItemStack, nearbyItemStack)) {
                    int totalAmount = newItemStack.getAmount() + nearbyItemStack.getAmount();
                    int maxStackSize = newItemStack.getMaxStackSize();

                    if (totalAmount <= maxStackSize) {
                        // Merge items into one stack
                        newItemStack.setAmount(totalAmount);
                        nearbyItem.remove();
                    } else {
                        // Fill the stack to the maximum and reduce the amount of the nearby item
                        newItemStack.setAmount(maxStackSize);
                        nearbyItemStack.setAmount(totalAmount - maxStackSize);
                    }
                }
            }
        });
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Item newItem = event.getEntity();
        // Create a repeating task to check for the item's velocity.
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if the item is resting on the ground (velocity is near zero).
                if (newItem.getVelocity().lengthSquared() < 0.01) {
                    this.cancel();
                    findNearbyItemsAndMerge(newItem);
                }
            }
        }.runTaskTimer(RunicItems.getInstance(), 0, 10L);
    }

}
