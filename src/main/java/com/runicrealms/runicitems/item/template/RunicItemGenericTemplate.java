package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.RunicItemGeneric;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;

import java.util.List;

public class RunicItemGenericTemplate extends RunicItemTemplate {

    private List<String> lore;

    public RunicItemGenericTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags,
                                    List<String> lore) {
        super(id, displayableItem, tags);
        this.lore = lore;
    }

    @Override
    public RunicItemGeneric generateItem() {
        return new RunicItemGeneric(
                this.id, this.displayableItem, this.tags,
                this.lore
        );
    }

    public List<String> getLore() {
        return this.lore;
    }

}
