package com.runicrealms.plugin.runicitems.item.template;

import com.runicrealms.plugin.runicitems.item.RunicItemBook;
import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RunicItemBookTemplate extends RunicItemTemplate {

    private final List<String> lore;
    private final String author;
    private final List<String> pages;

    public RunicItemBookTemplate(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data,
                                 List<String> lore, String author, List<String> pages) {
        super(id, displayableItem, tags, data);
        this.lore = lore;
        this.author = author;
        this.pages = new ArrayList<>();
        for (String page : pages) {
            this.pages.add(ChatColor.translateAlternateColorCodes('&', page));
        }
    }

    @Override
    public RunicItemBook generateItem(int count, long id, List<RunicItemTag> tags, Map<String, String> data) {
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

    public List<String> getPages() {
        return this.pages;
    }

}
