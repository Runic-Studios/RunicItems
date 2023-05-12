package com.runicrealms.runicitems.listeners;

import com.runicrealms.runicitems.util.NBTUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerMTIListener implements Listener {

    /**
     * Custom handler for when players MOVE_TO_OTHER_INVENTORY (shift-click)
     * This event handles the instance where the player's own inventory is open
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMoveToOtherInventory(InventoryClickEvent event) {
        // Only listen for shift-click
        if (event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) return;
        // Only listen for when the player's own inventory is open
        if (!(event.getView().getTopInventory() instanceof CraftingInventory)) return;
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory targetInventory;
        int loopStart = 0;
        int loopEnd;
        targetInventory = player.getInventory();
        if (event.getSlot() >= 9) { // Hotbar
            loopEnd = 9; // only targets items in hotbar
        } else {
            loopStart = 9;
            loopEnd = player.getInventory().getSize();
        }

        if (event.isCancelled()) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
            return;

        int amountLeft = event.getCurrentItem().getAmount();
        for (int i = loopStart; i < loopEnd; i++) {
            ItemStack item = targetInventory.getContents()[i];
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getAmount() == item.getMaxStackSize()) continue;
            if (!NBTUtil.isNBTSimilar(item, event.getCurrentItem(), false, false)) continue;
            // Check if the current item can fit into the item stack
            if (item.getAmount() + amountLeft <= item.getMaxStackSize()) {
                // If it can fit, add the amount to the item stack and remove the current item from the clicked inventory
                item.setAmount(item.getAmount() + amountLeft);
                clickedInventory.remove(event.getCurrentItem());
                event.setCurrentItem(null);
                event.setCancelled(true);
                // Set amount left to 0 and break out of the loop
                amountLeft = 0;
                break;
            } else {
                // If it can't fit, calculate the amount that can be added to the item stack
                int addedAmount = item.getMaxStackSize() - item.getAmount();
                // Subtract the added amount from the amount left
                amountLeft -= addedAmount;
                item.setAmount(item.getMaxStackSize());
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
