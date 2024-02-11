package com.runicrealms.plugin.runicitems.loot;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface LootHolder {

    /**
     * Gets the min level of items that a loot holder should generate for a given player.
     * This is currently used exclusively for script item generation.
     */
    int getItemMinLevel(@NotNull Player player);

    /**
     * Gets the max level of items that a loot holder should generate for a given player.
     * This is currently used exclusively for script item generation.
     */

    int getItemMaxLevel(@NotNull Player player);

}
