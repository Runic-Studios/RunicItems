package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;

import java.util.List;

public class RunicItemWeaponTemplate extends RunicItemTemplate {

    private RunicItemStatRange damageRange;
    private int level;
    private RunicItemRarity rarity;

    public RunicItemWeaponTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags,
                                   RunicItemStatRange damageRange,
                                   int level, RunicItemRarity rarity) {
        super(id, displayableItem, tags);
        this.damageRange = damageRange;
        this.level = level;
        this.rarity = rarity;
    }

    @Override
    public RunicItem generateItem() {
        return new RunicItemWeapon(this.id, this.displayableItem, this.tags, this.damageRange, this.level, this.rarity);
    }

}
