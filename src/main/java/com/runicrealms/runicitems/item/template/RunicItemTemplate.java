package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;

import java.util.List;
import java.util.Map;

public abstract class RunicItemTemplate {

    protected final String id;
    protected final DisplayableItem displayableItem;
    protected final List<RunicItemTag> tags;
    protected final Map<String, Object> data;

    public RunicItemTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data) {
        this.id = id;
        this.displayableItem = displayableItem;
        this.tags = tags;
        this.data = data;
    }

    public abstract RunicItem generateItem(int count, long id, List<RunicItemTag> tags, Map<String, Object> data);

    public String getId() {
        return this.id;
    }

    public DisplayableItem getDisplayableItem() {
        return this.displayableItem;
    }

    public List<RunicItemTag> getTags() {
        return this.tags;
    }

    public Map<String, Object> getData() {
        return this.data;
    }

}
