package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.item.inventory.RunicItemOwner;
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

    public RunicItemGeneric(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data, int count, long id, RunicItemOwner itemOwner,
                            Map<ClickTrigger, String> triggers, List<String> lore) {
        super(templateId, displayableItem, tags, data, count, id, itemOwner, () -> new ItemLoreSection[] {new ItemLoreSection(lore)});
        this.lore = lore;
        this.triggers = triggers;
    }

    public RunicItemGeneric(RunicItemGenericTemplate template, int count, long id, RunicItemOwner itemOwner) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id, itemOwner,
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