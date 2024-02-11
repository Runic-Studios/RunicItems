package com.runicrealms.plugin.runicitems.loot.chest;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegenerativeLootChest extends LootChest {

    private final int regenerationTime;

    public RegenerativeLootChest(
            @NotNull LootChestPosition location,
            @NotNull LootChestTemplate lootChestTemplate,
            @NotNull LootChestConditions conditions,
            int minLevel,
            int itemMinLevel, int itemMaxLevel,
            int regenerationTime,
            @NotNull String inventoryTitle,
            @Nullable String modelID) {
        super(location, lootChestTemplate, conditions, minLevel, itemMinLevel, itemMaxLevel, inventoryTitle, modelID);
        this.regenerationTime = regenerationTime;
    }

    public int getRegenerationTime() {
        return this.regenerationTime;
    }

    @Override
    public boolean shouldUpdateDisplay() {
        return true;
    }

    @Override
    public void onOpen(@NotNull Player player) {
        //no need to do anything extra
    }
}
