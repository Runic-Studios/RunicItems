package com.runicrealms.plugin.runicitems.item.template;

import com.runicrealms.plugin.runicitems.item.RunicItemGem;
import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.item.stats.GemBonus;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;

import java.util.List;
import java.util.Map;

public class RunicItemGemTemplate extends RunicItemTemplate {

    private final int tier;
    private final Stat mainStat;

    public RunicItemGemTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags,
                                Map<String, String> data, int tier, Stat mainStat) {
        super(id, displayableItem, tags, data);
        this.tier = tier;
        this.mainStat = mainStat;
    }

    @Override
    public RunicItemGem generateItem(int count, long id, List<RunicItemTag> tags, Map<String, String> data) {
        if (tags == null) tags = this.tags;
        if (data == null) data = this.data;
        return new RunicItemGem(this.id, displayableItem, tags, data, count, id, new GemBonus(this.mainStat, this.tier));
    }

    public int getTier() {
        return this.tier;
    }

    public Stat getMainStat() {
        return this.mainStat;
    }

}