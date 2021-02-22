package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.RunicItemBook;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RunicItemBookTemplate extends RunicItemTemplate {

    private final List<String> lore;
    private final String author;
    private final Collection<String> pages;

    public RunicItemBookTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data,
                                    List<String> lore, String author, Collection<String> pages) {
        super(id, displayableItem, tags, data);
        this.lore = lore;
        this.author = author;
        this.pages = pages;
    }

    @Override
    public RunicItemBook generateItem(int count, long id, List<RunicItemTag> tags, Map<String, Object> data) {
        if (tags == null) tags = this.tags;
        if (data == null) data = this.data;
        return new RunicItemBook(
                this.id, this.displayableItem, tags, data, count, id,
                this.lore, this.author, this.pages
        );
    }

    public List<String> getLore() {
        return this.lore;
    }

    public String getAuthor() {
        return this.author;
    }

    public Collection<String> getPages() {
        return this.pages;
    }

}
