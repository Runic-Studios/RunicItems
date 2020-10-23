package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.RunicItemGeneric;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ClickTrigger;

import java.util.List;
import java.util.Map;

public class RunicItemGenericTemplate extends RunicItemTemplate {

    private List<String> lore;
    private Map<ClickTrigger, String> triggers;

    public RunicItemGenericTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data,
                                    Map<ClickTrigger, String> triggers, List<String> lore) {
        super(id, displayableItem, tags, data);
        this.lore = lore;
        this.triggers = triggers;
    }

    @Override
    public RunicItemGeneric generateItem(int count) {
        return new RunicItemGeneric(
                this.id, this.displayableItem, this.tags, this.data, count,
                this.triggers, this.lore
        );
    }

    public List<String> getLore() {
        return this.lore;
    }

    public Map<ClickTrigger, String> getTriggers() {
        return this.triggers;
    }

}
