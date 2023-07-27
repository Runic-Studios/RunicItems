package com.runicrealms.plugin.runicitems.api;

import com.runicrealms.plugin.runicitems.model.InventoryData;

import java.util.UUID;

public interface DataAPI {

    /**
     * Creates an InventoryData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid       of player who is attempting to load their data
     * @param slotToLoad which character slot to load, else -1 if all should be loaded
     * @return an InventoryData object
     */
    InventoryData loadInventoryData(UUID uuid, int slotToLoad);

}
