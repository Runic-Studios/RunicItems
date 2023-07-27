package com.runicrealms.plugin.runicitems.listeners;

import com.runicrealms.plugin.runicitems.RunicItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;

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
        RunicItems.getInventoryAPI().combineItemStacks
                (
                        player,
                        event,
                        event.getCurrentItem(),
                        targetInventory,
                        loopStart,
                        loopEnd
                );
    }

}
