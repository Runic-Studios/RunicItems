package com.runicrealms.runicitems.listeners;

import org.bukkit.event.Listener;

/**
 * Used to combine similar ItemStacks on the floor
 */
public class ItemSpawnListener implements Listener {
    private static final double ITEM_MERGE_RADIUS = 1.5; // blocks

//    private void mergeItemStacks(Item spawnedItem) {
//        ItemStack spawnedStack = spawnedItem.getItemStack();
//        if (spawnedStack.getType() == Material.AIR) return;
//        Location location = spawnedItem.getLocation();
//        int maxStack = spawnedStack.getMaxStackSize();
//
//        // Check for nearby items of the same type
//        for (Item nearbyItem : location.getWorld().getNearbyEntitiesByType(Item.class, location, ITEM_MERGE_RADIUS, item -> !item.equals(spawnedItem))) {
//            ItemStack nearbyStack = nearbyItem.getItemStack();
//
//            // Check if the items are similar using your custom method
//            if (!RunicItemsAPI.isRunicItemSimilar(spawnedStack, nearbyStack)) continue;
//
//            // Combine the stacks if the total amount doesn't exceed the max stack size
////            Bukkit.broadcastMessage("new stack amount is " + spawnedStack.getAmount());
////            Bukkit.broadcastMessage("nearby stack amount is " + nearbyStack.getAmount());
//            int total = spawnedStack.getAmount() + nearbyStack.getAmount();
//            if (total <= maxStack) {
//                spawnedStack.setAmount(total);
//                nearbyItem.remove();
//            } else {
//                // If the total exceeds the max stack size, set the nearby item stack to the remaining amount
//                spawnedStack.setAmount(maxStack);
//                nearbyStack.setAmount(total - maxStack);
//                nearbyItem.setItemStack(nearbyStack);
//            }
//
//            // If the spawned item stack is now at the max stack size, stop checking for more items
//            if (spawnedStack.getAmount() == maxStack) {
//                break;
//            }
//        }
//
//        spawnedItem.setItemStack(spawnedStack);
//    }

//    @EventHandler(priority = EventPriority.HIGH)
//    public void onItemSpawn(ItemSpawnEvent event) {
//        Item spawnedItem = event.getEntity();
//        // Create a repeating task to check for the item's velocity.
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                // Check if the item is resting on the ground (velocity is near zero).
//                if (spawnedItem.getVelocity().lengthSquared() < 0.01) {
//                    this.cancel();
//                    mergeItemStacks(spawnedItem);
//                }
//            }
//        }.runTaskTimer(RunicItems.getInstance(), 0, 10L);
//    }

}
