package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.item.inventory.RunicItemOwner;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemBookTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RunicItemBook extends RunicItem {

    private final List<String> lore;
    private final String author;
    private final Collection<String> pages;

    public RunicItemBook(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data, int count,
                         List<String> lore, String author, Collection<String> pages) {
        super(templateId, displayableItem, tags, data, count, () -> new ItemLoreSection[] {new ItemLoreSection(lore)});
        this.lore = lore;
        this.author = author;
        this.pages = pages;
        if (this.getDisplayableItem().getMaterial() == Material.WRITTEN_BOOK) {
            BookMeta bookMeta = (BookMeta) this.currentItem.getItemMeta();
            bookMeta.setAuthor(author);
            for (String page : pages) {
                bookMeta.addPage(page);
            }
        }
    }

    public RunicItemBook(RunicItemBookTemplate template, int count) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count,
                template.getLore(), template.getAuthor(), template.getPages()
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

    @Override
    public void addSpecificItemToData(Data section) { }

}
