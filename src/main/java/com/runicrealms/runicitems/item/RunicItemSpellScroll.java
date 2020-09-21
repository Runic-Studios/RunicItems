package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.stats.RunicScrollItemSpellType;
import org.bukkit.Material;

import java.util.List;

public class RunicItemSpellScroll extends RunicItem {

    private RunicScrollItemSpellType spell;

    public RunicItemSpellScroll(String id, String itemName, Material material, short damage, List<RunicItemTag> tags, RunicScrollItemSpellType spell) {
        super(id, itemName, material, damage, tags);
        this.spell = spell;
    }

    public RunicScrollItemSpellType getSpellType() {
        return this.spell;
    }

}
