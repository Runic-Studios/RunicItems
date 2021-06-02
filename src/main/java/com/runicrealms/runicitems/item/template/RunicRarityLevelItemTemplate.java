package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;

import java.util.List;
import java.util.Map;

public abstract class RunicRarityLevelItemTemplate extends RunicItemTemplate {

    public RunicRarityLevelItemTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data) {
        super(id, displayableItem, tags, data);
    }

    public abstract int getLevel();

    public abstract RunicItemRarity getRarity();

}
