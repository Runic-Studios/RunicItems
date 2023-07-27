package com.runicrealms.plugin.runicitems.item.template;

import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;

import java.util.List;
import java.util.Map;

public abstract class RunicRarityLevelItemTemplate extends RunicItemTemplate {

    public RunicRarityLevelItemTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data) {
        super(id, displayableItem, tags, data);
    }

    public abstract int getLevel();

    public abstract RunicItemRarity getRarity();

}
