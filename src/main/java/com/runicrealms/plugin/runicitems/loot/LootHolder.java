package com.runicrealms.plugin.runicitems.loot;

public interface LootHolder {

    /**
     * Gets the min level of items that a loot holder should generate.
     * This is currently used exclusively for script item generation.
     */
    int getItemMinLevel();

    /**
     * Gets the max level of items that a loot holder should generate.
     * This is currently used exclusively for script item generation.
     */

    int getItemMaxLevel();

}
