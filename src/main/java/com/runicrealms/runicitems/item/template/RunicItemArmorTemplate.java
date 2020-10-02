package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.RunicItemArmor;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunicItemArmorTemplate extends RunicItemTemplate {

    private int level;
    private RunicItemRarity rarity;
    private LinkedHashMap<RunicItemStatType, RunicItemStatRange> stats;
    private int maxGemSlots;

    public RunicItemArmorTemplate(String id, String itemName, Material material, List<RunicItemTag> tags,
                                  int level, RunicItemRarity rarity, LinkedHashMap<RunicItemStatType, RunicItemStatRange> stats, int maxGemSlots) {
        super(id, itemName, material, id, tags);
        this.level = level;
        this.rarity = rarity;
        this.stats = stats;
        this.maxGemSlots = maxGemSlots;
    }

    @Override
    public void generateItem() {
        LinkedHashMap<RunicItemStatType, RunicItemStat> rolledStats = new LinkedHashMap<RunicItemStatType, RunicItemStat>();
        for (Map.Entry<RunicItemStatType, RunicItemStatRange> stat : this.stats.entrySet()) {
            rolledStats.put(stat.getKey(), new RunicItemStat(stat.getValue()));
        }
        return new RunicItemArmor(this.displayableItem, this.id, this.tags, this.level, this.rarity, rolledStats, this.maxGemSlots);
    }
}
