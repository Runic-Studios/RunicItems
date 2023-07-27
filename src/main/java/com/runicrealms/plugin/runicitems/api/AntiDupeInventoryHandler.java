package com.runicrealms.plugin.runicitems.api;

import org.bukkit.entity.Player;

/**
 * Used by plugins that activate anti-dupe for a specific inventory that the player may have opened.
 */
public interface AntiDupeInventoryHandler {

    /**
     * @return True if the player is currently viewing the relevant inventory to check for dupes
     */
    boolean isViewingInventory(Player player);

}
