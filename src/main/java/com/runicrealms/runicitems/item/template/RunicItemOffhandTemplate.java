package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.RunicItemOffhand;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunicItemOffhandTemplate extends RunicItemTemplate {

    private LinkedHashMap<RunicItemStatType, RunicItemStatRange> stats;
    private int level;
    private RunicItemRarity rarity;

    public RunicItemOffhandTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags,
                                    LinkedHashMap<RunicItemStatType, RunicItemStatRange> stats,
                                    int level, RunicItemRarity rarity) {
        super(id, displayableItem, tags);
        this.stats = stats;
        this.level = level;
        this.rarity = rarity;
    }

    @Override
    public RunicItemOffhand generateItem() {
        LinkedHashMap<RunicItemStatType, RunicItemStat> rolledStats = new LinkedHashMap<RunicItemStatType, RunicItemStat>();
        for (Map.Entry<RunicItemStatType, RunicItemStatRange> stat : this.stats.entrySet()) {
            rolledStats.put(stat.getKey(), new RunicItemStat(stat.getValue()));
        }
        return new RunicItemOffhand(
                this.id, this.displayableItem, this.tags,
                rolledStats,
                this.level, this.rarity
        );
    }

    public LinkedHashMap<RunicItemStatType, RunicItemStatRange> getStats() {
        return this.stats;
    }

    public int getLevel() {
        return this.level;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

}
