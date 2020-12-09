package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemGenericTemplate;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;

import java.util.List;
import java.util.Map;

public class RunicItemGeneric extends RunicItem {

    private final List<String> lore;
    private final Map<ClickTrigger, String> triggers;

    public RunicItemGeneric(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data, int count,
                            Map<ClickTrigger, String> triggers, List<String> lore) {
        super(id, displayableItem, tags, data, count, () -> new ItemLoreSection[] {new ItemLoreSection(lore)});
        this.lore = lore;
        this.triggers = triggers;
    }

    public RunicItemGeneric(RunicItemGenericTemplate template, int count) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count,
                template.getTriggers(), template.getLore()
        );
    }

    public List<String> getLore() {
        return this.lore;
    }

    public Map<ClickTrigger, String> getTriggers() {
        return this.triggers;
    }

    @Override
    public void addSpecificItemToData(Data section) {}

    // TODO on click check for generic item then check for triggers

}