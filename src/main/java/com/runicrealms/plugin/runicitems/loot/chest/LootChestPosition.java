package com.runicrealms.plugin.runicitems.loot.chest;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LootChestPosition {

    private final Location location;
    private final BlockFace direction;

    public LootChestPosition(@NotNull Location location, @NotNull BlockFace direction) {
        this.location = location;
        this.direction = direction;
    }

    public Location getLocation() {
        return this.location;
    }

    public BlockFace getDirection() {
        return this.direction;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LootChestPosition lootChestPosition) && lootChestPosition.getLocation().equals(this.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.location, this.direction);
    }
}
