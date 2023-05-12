package com.runicrealms.runicitems.api;

import com.runicrealms.runicitems.util.NBTUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Provides methods for handling inventories and click events using our custom items system
 *
 * @author Skyfallin
 */
public interface InventoryAPI {

    default void combineItemStacks(Player player, InventoryClickEvent event, ItemStack currentItem, Inventory targetInventory) {
        combineItemStacks(player, event, currentItem, targetInventory, 0, targetInventory.getSize());
    }

    /**
     * Attempts to replicate shift-click logic from vanilla MC accurately with RunicItems
     *
     * @param player          who shift-clicked
     * @param currentItem     the current item in the inventory
     * @param targetInventory the inventory the items will be moved to
     * @param loopStart       start of slot to iterate over, used to handle hotbar + custom inventories separately
     * @param loopEnd         end of loop, used to stop early when dealing with hotbars
     */
    default void combineItemStacks(Player player, InventoryClickEvent event, ItemStack currentItem, Inventory targetInventory, int loopStart, int loopEnd) {
        if (event.getCurrentItem() == null) return;
        int amountLeft = currentItem.getAmount();
        for (int i = loopStart; i < loopEnd; i++) {
            ItemStack item = targetInventory.getContents()[i];
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getAmount() == item.getMaxStackSize()) continue;
            if (!NBTUtil.isNBTSimilar(item, currentItem, false, false)) continue;
            // Check if the current item can fit into the item stack
            if (item.getAmount() + amountLeft <= item.getMaxStackSize()) {
                // If it can fit, add the amount to the item stack and remove the current item from the clicked inventory
                item.setAmount(item.getAmount() + amountLeft);
                // If the entire stack was moved, remove the current item
                if (amountLeft == currentItem.getAmount()) {
                    event.setCurrentItem(null); // IMPORTANT: Removes ALL INSTANCES of the stack in the player's inventory
                } else {
                    // If not all the stack was moved, reduce the amount of the current item
                    event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - amountLeft);
                }
                event.setCancelled(true);
                // Set amount left to 0 and break out of the loop
                amountLeft = 0;
                break;
            } else {
                // If it can't fit, calculate the amount that can be added to the item stack
                int addedAmount = item.getMaxStackSize() - item.getAmount();
                // Add the amount that can be added to the item stack
                item.setAmount(item.getMaxStackSize());
                // Subtract the added amount from the amount left
                amountLeft -= addedAmount;
                // Reduce the amount of the current item
                event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - addedAmount);
            }
        }
        // If there's still some amount left, set the amount of the current item to the amount left
        if (amountLeft > 0) {
            event.getCurrentItem().setAmount(amountLeft);
        }
        // Update the player's inventory to reflect the changes
        player.updateInventory();
    }

}
