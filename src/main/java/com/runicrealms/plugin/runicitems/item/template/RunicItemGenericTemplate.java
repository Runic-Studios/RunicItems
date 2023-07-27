package com.runicrealms.plugin.runicitems.item.template;

import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.runicitems.item.RunicItemGeneric;
import com.runicrealms.plugin.runicitems.item.util.ClickTrigger;
import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RunicItemGenericTemplate extends RunicItemTemplate {

    private final List<String> lore;
    private final Map<ClickTrigger, String> triggers;

    public RunicItemGenericTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data,
                                    Map<ClickTrigger, String> triggers, List<String> lore) {
        super(id, displayableItem, tags, data);
        this.lore = lore;
        this.triggers = triggers;
    }

    @Override
    public RunicItemGeneric generateItem(int count, long id, List<RunicItemTag> tags, Map<String, String> data) {
        if (tags == null) tags = this.tags;
        if (data == null) data = this.data;
        return new RunicItemGeneric(
                this.id, this.displayableItem, tags, data, count, id,
                this.triggers, this.lore
        );
    }

    public List<String> getLore() {
        List<String> formattedLore = new ArrayList<>();
        for (String s : this.lore) {
            formattedLore.add(ColorUtil.format(s));
        }
        return formattedLore;
    }

    public Map<ClickTrigger, String> getTriggers() {
        return this.triggers;
    }

}
