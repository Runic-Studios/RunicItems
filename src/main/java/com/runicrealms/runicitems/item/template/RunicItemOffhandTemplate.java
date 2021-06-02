package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.Stat;
import com.runicrealms.runicitems.item.RunicItemOffhand;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunicItemOffhandTemplate extends RunicRarityLevelItemTemplate {

    private final LinkedHashMap<Stat, RunicItemStatRange> stats;
    private final int level;
    private final RunicItemRarity rarity;

    public RunicItemOffhandTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data,
                                    LinkedHashMap<Stat, RunicItemStatRange> stats,
                                    int level, RunicItemRarity rarity) {
        super(id, displayableItem, tags, data);
        this.stats = stats;
        this.level = level;
        this.rarity = rarity;
    }

    @Override
    public RunicItemOffhand generateItem(int count, long id, List<RunicItemTag> tags, Map<String, String> data) {
        LinkedHashMap<Stat, RunicItemStat> rolledStats = new LinkedHashMap<>();
        for (Map.Entry<Stat, RunicItemStatRange> stat : this.stats.entrySet()) {
            rolledStats.put(stat.getKey(), new RunicItemStat(stat.getValue()));
        }
        if (tags == null) tags = this.tags;
        if (data == null) data = this.data;
        return new RunicItemOffhand(
                this.id, this.displayableItem, tags, data, count, id,
                rolledStats,
                this.level, this.rarity
        );
    }

    public LinkedHashMap<Stat, RunicItemStatRange> getStats() {
        return this.stats;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public RunicItemRarity getRarity() {
        return this.rarity;
    }

}
