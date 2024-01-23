package com.runicrealms.plugin.runicitems.item.template;

import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.item.ClassRequirementHolder;
import com.runicrealms.plugin.runicitems.item.LevelRequirementHolder;
import com.runicrealms.plugin.runicitems.item.RunicItemArmor;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerk;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkType;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemStat;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;
import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;
import com.runicrealms.plugin.runicitems.item.util.RunicItemClass;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RunicItemArmorTemplate extends RunicRarityLevelItemTemplate implements ClassRequirementHolder, LevelRequirementHolder {

    private final int level;
    private final RunicItemRarity rarity;
    private final int health;
    private final LinkedHashMap<Stat, RunicItemStatRange> stats;
    private final int maxGemSlots;
    private final LinkedHashMap<String, Integer> defaultItemPerks;
    private final RunicItemClass runicClass;

    public RunicItemArmorTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data,
                                  int health, LinkedHashMap<Stat, RunicItemStatRange> stats, int maxGemSlots, LinkedHashMap<String, Integer> defaultItemPerks,
                                  int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(id, displayableItem, tags, data);
        this.level = level;
        this.rarity = rarity;
        this.health = health;
        this.stats = stats;
        this.maxGemSlots = maxGemSlots;
        this.defaultItemPerks = defaultItemPerks;
        this.runicClass = runicClass;
    }

    @Override
    public RunicItemArmor generateItem(int count, long id, List<RunicItemTag> tags, Map<String, String> data) {
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
        return new RunicItemArmor(
                this.id, displayableItem, tags, data, count, id,
                this.health, rolledStats, new LinkedList<>(), this.maxGemSlots, itemPerks,
                this.level, this.rarity, this.runicClass
        );
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public RunicItemRarity getRarity() {
        return this.rarity;
    }

    public int getHealth() {
        return this.health;
    }

    public LinkedHashMap<Stat, RunicItemStatRange> getStats() {
        return this.stats;
    }

    public int getMaxGemSlots() {
        return this.maxGemSlots;
    }

    @Override
    public RunicItemClass getRunicClass() {
        return this.runicClass;
    }

}
