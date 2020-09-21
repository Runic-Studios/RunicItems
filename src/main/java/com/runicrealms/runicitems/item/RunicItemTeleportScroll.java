package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;

public class RunicItemTeleportScroll extends RunicItem {

    private Location teleportLocation;

    public RunicItemTeleportScroll(String id, String itemName, Material material, short damage, List<RunicItemTag> tags, Location teleportLocation) {
        super(id, itemName, material, damage, tags);
        this.teleportLocation = teleportLocation;
    }

    public Location getTeleportLocation() {
        return this.teleportLocation;
    }

}
