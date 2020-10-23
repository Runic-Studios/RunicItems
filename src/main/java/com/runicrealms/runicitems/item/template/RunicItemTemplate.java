package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;

import java.util.List;
import java.util.Map;

public abstract class RunicItemTemplate {

    protected String id;
    protected DisplayableItem displayableItem;
    protected List<RunicItemTag> tags;
    protected Map<String, Object> data;

    public RunicItemTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data) {
        this.id = id;
        this.displayableItem = displayableItem;
        this.tags = tags;
        this.data = data;
    }

    public abstract RunicItem generateItem(int count);

    public String getId() {
        return this.id;
    }

    public DisplayableItem getDisplayableItem() {
        return this.displayableItem;
    }

    public List<RunicItemTag> getTags() {
        return this.tags;
    }

}
