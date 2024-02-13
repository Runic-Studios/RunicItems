package com.runicrealms.plugin.runicitems.loot.chest;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class BossTimedLoot extends TimedLoot {

    private final String mmBossID;
    private final double lootDamageThreshold; // percentage, 0 means don't track damage
    private final int lootRange; // How close players need to be to the boss to gain loot credit, -1 if doesn't matter
    private final Location complete; //the location to teleport the player back to once the boss is killed

    public BossTimedLoot(@NotNull TimedLootChest lootChest, @NotNull String mmBossID, double lootDamageThreshold, int lootRange, @NotNull Location complete) {
        super(lootChest);
        this.mmBossID = mmBossID;
        if (MythicBukkit.inst().getAPIHelper().getMythicMob(mmBossID) == null) {
            throw new IllegalArgumentException("Boss timed loot has invalid MM ID: " + mmBossID);
        }
        this.lootDamageThreshold = lootDamageThreshold;
        this.lootRange = lootRange;
        this.complete = complete;
    }

    @NotNull
    public String getMmBossID() {
        return this.mmBossID;
    }

    public double getLootDamageThreshold() {
        return this.lootDamageThreshold;
    }

    public int getLootRange() {
        return this.lootRange;
    }

    @NotNull
    public Location getComplete() {
        return this.complete;
    }
}
