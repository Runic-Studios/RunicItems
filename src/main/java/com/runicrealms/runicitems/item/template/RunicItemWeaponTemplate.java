package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.RunicItemWeapon;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.RunicItemClass;

import java.util.List;

public class RunicItemWeaponTemplate extends RunicItemTemplate {

    private RunicItemStatRange damageRange;
    private int level;
    private RunicItemRarity rarity;
    private RunicItemClass runicClass;

    public RunicItemWeaponTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags,
                                   RunicItemStatRange damageRange,
                                   int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(id, displayableItem, tags);
        this.damageRange = damageRange;
        this.level = level;
        this.rarity = rarity;
        this.runicClass = runicClass;
    }

    @Override
    public RunicItemWeapon generateItem() {
        return new RunicItemWeapon(
                this.id, this.displayableItem, this.tags,
                this.damageRange,
                this.level, this.rarity, this.runicClass
        );
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
