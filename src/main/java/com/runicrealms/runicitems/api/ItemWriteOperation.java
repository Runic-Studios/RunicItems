package com.runicrealms.runicitems.api;

import com.runicrealms.plugin.rdb.api.WriteCallback;
import com.runicrealms.runicitems.item.RunicItem;

import java.util.UUID;

/**
 * Used for efficient updating of mongo document fields
 */
public interface ItemWriteOperation {

    /**
     * ?
     *
     * @param uuid
     * @param slot
     * @param newValue
     * @param callback
     */
    void updateInventoryData(UUID uuid, int slot, RunicItem[] newValue, WriteCallback callback);

}
