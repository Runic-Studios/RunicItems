package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class RunicItemArmor extends RunicItem {

    private Map<RunicItemStatType, RunicItemStat> stats;
    private int maxGemSlots;

    public RunicItemArmor(String id, String itemName, Material material, short damage, List<RunicItemTag> tags, Map<RunicItemStatType, RunicItemStat> stats, int maxGemSlots) {
        super(id, itemName, material, damage, tags);
        this.stats = stats;
        this.maxGemSlots = maxGemSlots;
    }

    public Map<RunicItemStatType, RunicItemStat> getStats() {
        return this.stats;
    }

    public int getMaxGemSlots() {
        return this.maxGemSlots;
    }

}
