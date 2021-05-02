package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.RunicItemArmor;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.plugin.player.stat.PlayerStatEnum;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.RunicItemClass;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunicItemArmorTemplate extends RunicItemTemplate {

    private final int level;
    private final RunicItemRarity rarity;
    private final int health;
    private final LinkedHashMap<PlayerStatEnum, RunicItemStatRange> stats;
    private final int maxGemSlots;
    private final RunicItemClass runicClass;

    public RunicItemArmorTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data,
                                  int health, LinkedHashMap<PlayerStatEnum, RunicItemStatRange> stats, int maxGemSlots,
                                  int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(id, displayableItem, tags, data);
        this.level = level;
        this.rarity = rarity;
        this.health = health;
        this.stats = stats;
        this.maxGemSlots = maxGemSlots;
        this.runicClass = runicClass;
    }

    @Override
    public RunicItemArmor generateItem(int count, long id, List<RunicItemTag> tags, Map<String, String> data) {
        LinkedHashMap<PlayerStatEnum, RunicItemStat> rolledStats = new LinkedHashMap<PlayerStatEnum, RunicItemStat>();
        for (Map.Entry<PlayerStatEnum, RunicItemStatRange> stat : this.stats.entrySet()) {
            rolledStats.put(stat.getKey(), new RunicItemStat(stat.getValue()));
        }
        if (tags == null) tags = this.tags;
        if (data == null) data = this.data;
        return new RunicItemArmor(
                this.id, displayableItem, tags, data, count, id,
                this.health, rolledStats, new ArrayList<LinkedHashMap<PlayerStatEnum, Integer>>(), this.maxGemSlots,
                this.level, this.rarity, this.runicClass
        );
    }

    public int getLevel() {
        return this.level;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

    public int getHealth() {
        return this.health;
    }

    public LinkedHashMap<PlayerStatEnum, RunicItemStatRange> getStats() {
        return this.stats;
    }

    public int getMaxGemSlots() {
        return this.maxGemSlots;
    }

    public RunicItemClass getRunicClass() {
        return this.runicClass;
    }

}
