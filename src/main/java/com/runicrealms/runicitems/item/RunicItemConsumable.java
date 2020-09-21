package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class RunicItemConsumable extends RunicItem {

    private Map<RunicItemStatType, RunicItemStatRange> stats;
    private int charges;

    public RunicItemConsumable(String id, String itemName, Material material, short damage, List<RunicItemTag> tags, Map<RunicItemStatType, RunicItemStatRange> stats, int charges) {
        super(id, itemName, material, damage, tags);
        this.stats = stats;
        this.charges = charges;
    }

    public Map<RunicItemStatType, RunicItemStatRange> getStats() {
        return this.stats;
    }

    public int getCharges() {
        return this.charges;
    }
}
