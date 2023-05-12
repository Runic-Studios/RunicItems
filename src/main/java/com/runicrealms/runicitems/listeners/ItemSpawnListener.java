package com.runicrealms.runicitems.listeners;

import org.bukkit.event.Listener;

/**
 * Used to combine similar ItemStacks on the floor
 */
public class ItemSpawnListener implements Listener {
//    private static final double ITEM_MERGE_RADIUS = 1.5; // blocks
//
//    private void findNearbyItemsAndMerge(Item newItem) {
//        if (newItem == null) return;
//        ItemStack newItemStack = newItem.getItemStack();
//        if (newItemStack.getType() == Material.AIR) return;
//        newItem.getNearbyEntities(ITEM_MERGE_RADIUS, ITEM_MERGE_RADIUS, ITEM_MERGE_RADIUS).forEach(entity -> {
//            if (entity instanceof Item nearbyItem) {
//                ItemStack nearbyItemStack = nearbyItem.getItemStack();
//
//                // Check if items are the same and can be merged
//                if (RunicItemsAPI.isRunicItemSimilar(newItemStack, nearbyItemStack)) {
//                    int totalAmount = newItemStack.getAmount() + nearbyItemStack.getAmount();
//                    int maxStackSize = newItemStack.getMaxStackSize();
//
//                    if (totalAmount <= maxStackSize) {
//                        // Merge items into one stack
//                        newItemStack.setAmount(totalAmount);
//                        nearbyItem.remove();
//                    } else {
//                        // Fill the stack to the maximum and reduce the amount of the nearby item
//                        newItemStack.setAmount(maxStackSize);
//                        nearbyItemStack.setAmount(totalAmount - maxStackSize);
//                    }
//                }
//            }
//        });
//    }
//
//    @EventHandler
//    public void onItemSpawn(ItemSpawnEvent event) {
//        Item newItem = event.getEntity();
//        // Create a repeating task to check for the item's velocity.
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                // Check if the item is resting on the ground (velocity is near zero).
//                if (newItem.getVelocity().lengthSquared() < 0.01) {
//                    this.cancel();
//                    findNearbyItemsAndMerge(newItem);
//                }
//            }
//        }.runTaskTimer(RunicItems.getInstance(), 0, 10L);
//    }

}
