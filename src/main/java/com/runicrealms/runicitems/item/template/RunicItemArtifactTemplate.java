package com.runicrealms.runicitems.item.template;

 import com.runicrealms.runicitems.item.RunicItemArtifact;
 import com.runicrealms.runicitems.item.stats.RunicArtifactAbility;
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

public class RunicItemArtifactTemplate extends RunicItemTemplate {

    private RunicArtifactAbility ability;
    private RunicItemStatRange damageRange;
    private LinkedHashMap<RunicItemStatType, RunicItemStatRange> stats;
    private int level;
    private RunicItemRarity rarity;
    private RunicItemClass runicClass;

    public RunicItemArtifactTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data,
                                     RunicArtifactAbility ability, RunicItemStatRange damageRange, LinkedHashMap<RunicItemStatType, RunicItemStatRange> stats,
                                     int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(id, displayableItem, tags, data);
        this.ability = ability;
        this.damageRange = damageRange;
        this.stats = stats;
        this.level = level;
        this.rarity = rarity;
        this.runicClass = runicClass;
    }

    @Override
    public RunicItemArtifact generateItem(int count) {
        LinkedHashMap<RunicItemStatType, RunicItemStat> rolledStats = new LinkedHashMap<RunicItemStatType, RunicItemStat>();
        for (Map.Entry<RunicItemStatType, RunicItemStatRange> stat : this.stats.entrySet()) {
            rolledStats.put(stat.getKey(), new RunicItemStat(stat.getValue()));
        }
        return new RunicItemArtifact(
                this.id, this.displayableItem, this.tags, this.data, count,
                this.ability, this.damageRange, rolledStats,
                this.level, this.rarity, this.runicClass
        );
    }

    public RunicArtifactAbility getAbility() {
        return this.ability;
    }

    public RunicItemStatRange getDamageRange() {
        return this.damageRange;
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

    public RunicItemClass getRunicClass() {
        return this.runicClass;
    }

}
