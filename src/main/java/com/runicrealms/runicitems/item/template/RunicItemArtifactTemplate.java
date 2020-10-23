package com.runicrealms.runicitems.item.template;

 import com.runicrealms.runicitems.item.RunicItemArtifact;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
 import com.runicrealms.runicitems.item.stats.RunicSpell;
import com.runicrealms.runicitems.item.util.DefaultSpell;
import com.runicrealms.runicitems.item.util.DisplayableItem;
 import com.runicrealms.runicitems.item.util.RunicItemClass;
 import com.runicrealms.runicitems.item.util.SpellClickTrigger;

import java.util.LinkedHashMap;
import java.util.List;

public class RunicItemArtifactTemplate extends RunicItemTemplate {

    private DefaultSpell defaultSpell;
    private RunicItemStatRange damageRange;
    private int level;
    private RunicItemRarity rarity;
    private RunicItemClass runicClass;

    public RunicItemArtifactTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags,
                                     DefaultSpell defaultSpell, RunicItemStatRange damageRange,
                                     int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(id, displayableItem, tags);
        this.defaultSpell = defaultSpell;
        this.damageRange = damageRange;
        this.level = level;
        this.rarity = rarity;
        this.runicClass = runicClass;
    }

    @Override
    public RunicItemArtifact generateItem() {
        LinkedHashMap<SpellClickTrigger, RunicSpell> spells = new LinkedHashMap<SpellClickTrigger, RunicSpell>();
        spells.put(this.defaultSpell.getTrigger(), this.defaultSpell.getSpell());
        return new RunicItemArtifact(
                this.id, this.displayableItem, this.tags,
                spells, this.damageRange,
                this.level, this.rarity, this.runicClass
        );
    }

    public DefaultSpell getDefaultSpell() {
        return this.defaultSpell;
    }

    public RunicItemStatRange getDamageRange() {
        return this.damageRange;
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
