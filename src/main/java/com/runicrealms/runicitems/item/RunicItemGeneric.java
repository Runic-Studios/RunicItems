package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import org.bukkit.Material;

import java.util.List;

public class RunicItemGeneric extends RunicItem {

    public RunicItemGeneric(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, List<String> lore) {
        super(id, displayableItem, tags, () -> new ItemLoreSection[] {new ItemLoreSection(lore)});
    }

}