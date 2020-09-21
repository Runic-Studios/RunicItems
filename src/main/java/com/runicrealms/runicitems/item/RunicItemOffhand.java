package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class RunicItemOffhand extends RunicItem {

    private Map<RunicItemStatType, Integer> stats;

    public RunicItemOffhand(String id, String itemName, Material material, short damage, List<RunicItemTag> tags, Map<RunicItemStatType, Integer> stats) {
        super(id, itemName, material, damage, tags);
        this.stats = stats;
    }

    public Map<RunicItemStatType, Integer> getStats() {
        return this.stats;
    }
}
