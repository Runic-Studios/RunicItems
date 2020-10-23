package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.RunicItemArmor;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.RunicItemClass;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunicItemArmorTemplate extends RunicItemTemplate {

    private int level;
    private RunicItemRarity rarity;
    private LinkedHashMap<RunicItemStatType, RunicItemStatRange> stats;
    private int maxGemSlots;
    private RunicItemClass runicClass;

    public RunicItemArmorTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data,
                                  LinkedHashMap<RunicItemStatType, RunicItemStatRange> stats, int maxGemSlots,
                                  int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(id, displayableItem, tags, data);
        this.level = level;
        this.rarity = rarity;
        this.stats = stats;
        this.maxGemSlots = maxGemSlots;
        this.runicClass = runicClass;
    }

    @Override
    public RunicItemArmor generateItem(int count) {
        LinkedHashMap<RunicItemStatType, RunicItemStat> rolledStats = new LinkedHashMap<RunicItemStatType, RunicItemStat>();
        for (Map.Entry<RunicItemStatType, RunicItemStatRange> stat : this.stats.entrySet()) {
            rolledStats.put(stat.getKey(), new RunicItemStat(stat.getValue()));
        }
        return new RunicItemArmor(
                this.id, displayableItem, this.tags, this.data, count,
                rolledStats, new ArrayList<LinkedHashMap<RunicItemStatType, Integer>>(), this.maxGemSlots,
                this.level, this.rarity, this.runicClass
        );
    }

    public int getLevel() {
        return this.level;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

    public LinkedHashMap<RunicItemStatType, RunicItemStatRange> getStats() {
        return this.stats;
    }

    public int getMaxGemSlots() {
        return this.maxGemSlots;
    }

    public RunicItemClass getRunicClass() {
        return this.runicClass;
    }

}
