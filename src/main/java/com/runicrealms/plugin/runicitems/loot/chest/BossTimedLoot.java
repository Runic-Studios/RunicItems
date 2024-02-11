package com.runicrealms.plugin.runicitems.loot.chest;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class BossTimedLoot extends TimedLoot {

    private final String mmBossID;
    private final double lootDamageThreshold; // percentage, 0 means don't track damage
    private final Location complete; //the location to teleport the player back to once the boss is killed

    public BossTimedLoot(@NotNull TimedLootChest lootChest, @NotNull String mmBossID, double lootDamageThreshold, @NotNull Location complete) {
        super(lootChest);
        this.mmBossID = mmBossID;
        if (MythicBukkit.inst().getAPIHelper().getMythicMob(mmBossID) == null) {
            throw new IllegalArgumentException("Boss timed loot has invalid MM ID: " + mmBossID);
        }
        this.lootDamageThreshold = lootDamageThreshold;
        this.complete = complete;
    }

    @NotNull
    public String getMmBossID() {
        return this.mmBossID;
    }

    public double getLootDamageThreshold() {
        return this.lootDamageThreshold;
    }

    @NotNull
    public Location getComplete() {
        return this.complete;
    }
}
