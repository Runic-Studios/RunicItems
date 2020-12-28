package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.RunicItemWeapon;
import com.runicrealms.runicitems.item.inventory.RunicItemOwner;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.RunicItemClass;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunicItemWeaponTemplate extends RunicItemTemplate {

    private final RunicItemStatRange damageRange;
    private final LinkedHashMap<RunicItemStatType, RunicItemStatRange> stats;
    private final int level;
    private final RunicItemRarity rarity;
    private final RunicItemClass runicClass;

    public RunicItemWeaponTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data,
                                   RunicItemStatRange damageRange, LinkedHashMap<RunicItemStatType, RunicItemStatRange> stats,
                                   int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(id, displayableItem, tags, data);
        this.damageRange = damageRange;
        this.stats = stats;
        this.level = level;
        this.rarity = rarity;
        this.runicClass = runicClass;
    }

    @Override
    public RunicItemWeapon generateItem(int count, long id, RunicItemOwner itemOwner) {
        LinkedHashMap<RunicItemStatType, RunicItemStat> rolledStats = new LinkedHashMap<RunicItemStatType, RunicItemStat>();
        for (Map.Entry<RunicItemStatType, RunicItemStatRange> stat : this.stats.entrySet()) {
            rolledStats.put(stat.getKey(), new RunicItemStat(stat.getValue()));
        }
        return new RunicItemWeapon(
                this.id, this.displayableItem, this.tags, this.data, count, id, itemOwner,
                this.damageRange, rolledStats,
                this.level, this.rarity, this.runicClass
        );
    }

    public RunicItemStatRange getDamageRange() {
        return this.damageRange;
    }

    public LinkedHashMap<RunicItemStatType, RunicItemStatRange> getStats() {
        return stats;
    }

    public int getLevel() {
        return this.level;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

    public RunicItemClass getRunicClass() {
        return this.runicClass;
    }

}
