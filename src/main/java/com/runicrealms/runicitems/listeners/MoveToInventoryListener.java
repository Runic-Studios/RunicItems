package com.runicrealms.runicitems.listeners;

import com.runicrealms.runicitems.RunicItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;

public class MoveToInventoryListener implements Listener {

    /**
     * Custom handler for when players MOVE_TO_OTHER_INVENTORY (shift-click)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMoveToOtherInventory(InventoryClickEvent event) {
        // Only listen for shift-click
        if (event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) return;
        // Don't listen to player's own inventory (handled in PlayerMTIListener)
        if (event.getView().getTopInventory() instanceof CraftingInventory) return;
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory targetInventory = clickedInventory.equals(event.getView().getTopInventory()) ? event.getView().getBottomInventory() : event.getView().getTopInventory();
        if (event.isCancelled()) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
            return;
        RunicItems.getInventoryAPI().combineItemStacks
                (
                        player,
                        event,
                        event.getCurrentItem(),
                        targetInventory
                );
    }

}
