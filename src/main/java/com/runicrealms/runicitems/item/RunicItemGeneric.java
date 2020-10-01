package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import org.bukkit.Material;

import java.util.List;

public class RunicItemGeneric extends RunicItem {

    public RunicItemGeneric(String id, String itemName, Material material, short damage, List<RunicItemTag> tags, List<String> lore) {
        super(id, itemName, material, damage, tags, () -> new ItemLoreSection[] {new ItemLoreSection(lore)});
    }

}