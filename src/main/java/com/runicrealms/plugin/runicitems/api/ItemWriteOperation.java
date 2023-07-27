package com.runicrealms.plugin.runicitems.api;

import com.runicrealms.plugin.rdb.api.WriteCallback;
import com.runicrealms.plugin.runicitems.item.RunicItem;

import java.util.UUID;

/**
 * Used for efficient updating of mongo document fields
 */
public interface ItemWriteOperation {

    /**
     * Updates a single field of the mapped 'InventoryData' document object
     *
     * @param uuid     of the player
     * @param slot     of the character
     * @param newValue the new value for the field
     * @param callback a function to execute on main thread when write operation is complete
     */
    void updateInventoryData(UUID uuid, int slot, RunicItem[] newValue, WriteCallback callback);

}
