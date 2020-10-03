package com.runicrealms.runicitems.item.template;

 import com.runicrealms.runicitems.item.RunicItemArtifact;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.stats.RunicSpellType;
import com.runicrealms.runicitems.item.util.DefaultSpell;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.SpellClickTrigger;

import java.util.LinkedHashMap;
import java.util.List;

public class RunicItemArtifactTemplate extends RunicItemTemplate {

    private DefaultSpell defaultSpell;
    private RunicItemStatRange damageRange;
    private int level;
    private RunicItemRarity rarity;

    public RunicItemArtifactTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags,
                                     DefaultSpell defaultSpell, RunicItemStatRange damageRange,
                                     int level, RunicItemRarity rarity) {
        super(id, displayableItem, tags);
        this.defaultSpell = defaultSpell;
        this.damageRange = damageRange;
        this.level = level;
        this.rarity = rarity;
    }

    @Override
    public RunicItemArtifact generateItem() {
        LinkedHashMap<SpellClickTrigger, RunicSpellType> spells = new LinkedHashMap<SpellClickTrigger, RunicSpellType>();
        spells.put(this.defaultSpell.getTrigger(), this.defaultSpell.getSpellType());
        return new RunicItemArtifact(this.id, this.displayableItem, this.tags, spells, this.damageRange, this.level, this.rarity);
    }

}
