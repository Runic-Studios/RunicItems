package com.runicrealms.plugin.runicitems.item.template;

import com.runicrealms.plugin.runicitems.item.RunicItemDynamic;
import com.runicrealms.plugin.runicitems.item.RunicItemGeneric;
import com.runicrealms.plugin.runicitems.item.util.ClickTrigger;
import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;

import java.util.List;
import java.util.Map;

/**
 * This is for caching / storing YAML data
 */
public class RunicItemDynamicTemplate extends RunicItemGenericTemplate {

    private final int dynamicField;

    public RunicItemDynamicTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags,
                                    Map<String, String> data, Map<ClickTrigger, String> triggers, List<String> lore,
                                    int dynamicField) {
        super(id, displayableItem, tags, data, triggers, lore);
        this.dynamicField = dynamicField;
    }

    /*
    Called when RunicItemsAPI.generateItemFromTemplate() is called on a RunicItemDynamic
     */
    @Override
    public RunicItemGeneric generateItem(int count, long id, List<RunicItemTag> tags, Map<String, String> data) {
        if (tags == null) tags = this.tags;
        if (data == null) data = this.data;
        return new RunicItemDynamic(
                this.id, this.displayableItem, tags, data, count, id,
                this.getTriggers(), this.getLore()
        );
    }

    public int getDynamicField() {
        return dynamicField;
    }
}
