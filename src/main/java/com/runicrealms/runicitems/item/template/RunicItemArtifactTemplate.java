package com.runicrealms.runicitems.item.template;

 import com.runicrealms.runicitems.item.RunicItemArtifact;
 import com.runicrealms.runicitems.item.stats.RunicArtifactAbility;
 import com.runicrealms.runicitems.item.stats.RunicItemRarity;
 import com.runicrealms.runicitems.item.stats.RunicItemStat;
 import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
 import com.runicrealms.plugin.player.stat.PlayerStatEnum;
 import com.runicrealms.runicitems.item.stats.RunicItemTag;
 import com.runicrealms.runicitems.item.util.DisplayableItem;
 import com.runicrealms.runicitems.item.util.RunicItemClass;

 import java.util.LinkedHashMap;
import java.util.List;
 import java.util.Map;

public class RunicItemArtifactTemplate extends RunicItemTemplate {

    private final RunicArtifactAbility ability;
    private final RunicItemStatRange damageRange;
    private final LinkedHashMap<PlayerStatEnum, RunicItemStatRange> stats;
    private final int level;
    private final RunicItemRarity rarity;
    private final RunicItemClass runicClass;

    public RunicItemArtifactTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data,
                                     RunicArtifactAbility ability, RunicItemStatRange damageRange, LinkedHashMap<PlayerStatEnum, RunicItemStatRange> stats,
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
    public RunicItemArtifact generateItem(int count, long id, List<RunicItemTag> tags, Map<String, String> data) {
        LinkedHashMap<PlayerStatEnum, RunicItemStat> rolledStats = new LinkedHashMap<PlayerStatEnum, RunicItemStat>();
        for (Map.Entry<PlayerStatEnum, RunicItemStatRange> stat : this.stats.entrySet()) {
            rolledStats.put(stat.getKey(), new RunicItemStat(stat.getValue()));
        }
        if (tags == null) tags = this.tags;
        if (data == null) data = this.data;
        return new RunicItemArtifact(
                this.id, this.displayableItem, tags, data, count, id,
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

    public LinkedHashMap<PlayerStatEnum, RunicItemStatRange> getStats() {
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
