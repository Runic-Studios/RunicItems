package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.RunicItemArmor;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunicItemArmorTemplate extends RunicItemTemplate {

    private int level;
    private RunicItemRarity rarity;
    private LinkedHashMap<RunicItemStatType, RunicItemStatRange> stats;
    private int maxGemSlots;

    public RunicItemArmorTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags,
                                  LinkedHashMap<RunicItemStatType, RunicItemStatRange> stats, int maxGemSlots,
                                  int level, RunicItemRarity rarity) {
        super(id, displayableItem, tags);
        this.level = level;
        this.rarity = rarity;
        this.stats = stats;
        this.maxGemSlots = maxGemSlots;
    }

    @Override
    public RunicItemArmor generateItem() {
        LinkedHashMap<RunicItemStatType, RunicItemStat> rolledStats = new LinkedHashMap<RunicItemStatType, RunicItemStat>();
        for (Map.Entry<RunicItemStatType, RunicItemStatRange> stat : this.stats.entrySet()) {
            rolledStats.put(stat.getKey(), new RunicItemStat(stat.getValue()));
        }
        return new RunicItemArmor(this.id, displayableItem, this.tags, rolledStats, new LinkedHashMap<RunicItemStatType, Integer>(), this.maxGemSlots, this.level, this.rarity);
    }

}
