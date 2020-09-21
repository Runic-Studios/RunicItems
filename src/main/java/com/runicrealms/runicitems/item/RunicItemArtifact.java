package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicArtifactItemSpellType;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.Material;

import java.util.List;

public class RunicItemArtifact extends RunicItem {

    private RunicArtifactItemSpellType defaultSpell;
    private RunicItemStatRange damageRange;
    private int level;

    public RunicItemArtifact(String id, String itemName, Material material, short damage, List<RunicItemTag> tags, RunicArtifactItemSpellType defaultSpell, RunicItemStatRange damageRange) {
        super(id, itemName, material, damage, tags);
        this.defaultSpell = defaultSpell;
        this.damageRange = damageRange;
    }

    public RunicArtifactItemSpellType getDefaultSpell() {
        return this.defaultSpell;
    }

    public RunicItemStatRange getDamageRange() {
        return this.damageRange;
    }

    public int getRandomDamage() {
        return this.damageRange.getRandomValue();
    }

}
