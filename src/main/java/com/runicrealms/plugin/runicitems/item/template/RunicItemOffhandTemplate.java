package com.runicrealms.plugin.runicitems.item.template;

import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.item.LevelRequirementHolder;
import com.runicrealms.plugin.runicitems.item.RunicItemOffhand;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerk;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkType;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemStat;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;
import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class RunicItemOffhandTemplate extends RunicRarityLevelItemTemplate implements LevelRequirementHolder {

    private final LinkedHashMap<Stat, RunicItemStatRange> stats;
    private final LinkedHashMap<String, Integer> defaultItemPerks;
    private final int level;
    private final RunicItemRarity rarity;

    public RunicItemOffhandTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data,
                                    LinkedHashMap<Stat, RunicItemStatRange> stats, LinkedHashMap<String, Integer> defaultItemPerks,
                                    int level, RunicItemRarity rarity) {
        super(id, displayableItem, tags, data);
        this.stats = stats;
        this.defaultItemPerks = defaultItemPerks;
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
        LinkedHashSet<ItemPerk> itemPerks = new LinkedHashSet<>();
        for (String itemPerkIdentifier : defaultItemPerks.keySet()) {
            ItemPerkType type = RunicItems.getItemPerkManager().getType(itemPerkIdentifier);
            if (type == null) continue;
            int amount = defaultItemPerks.get(itemPerkIdentifier);
            if (amount <= 0) continue;
            itemPerks.add(new ItemPerk(type, amount));
        }
        return new RunicItemOffhand(
                this.id, this.displayableItem, tags, data, count, id,
                rolledStats, itemPerks,
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
