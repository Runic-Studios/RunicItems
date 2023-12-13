package com.runicrealms.plugin.runicitems.item;

import com.runicrealms.plugin.runicitems.TemplateManager;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;
import com.runicrealms.plugin.runicitems.item.template.RunicItemBookTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RunicItemBook extends RunicItem {

    private final List<String> lore;
    private final String author;
    private final List<String> pages;

    public RunicItemBook(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                         List<String> lore, String author, List<String> pages) {
        super(templateId, displayableItem, tags, data, count, id);
        this.lore = lore;
        this.author = author;
        this.pages = pages;

    }

    public RunicItemBook(RunicItemBookTemplate template, int count, long id) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getLore(), template.getAuthor(), template.getPages()
        );
    }

    public static RunicItemBook getFromItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemBookTemplate))
            throw new IllegalArgumentException("ItemStack is not a book item!");
        RunicItemBookTemplate template = (RunicItemBookTemplate) uncastedTemplate;
        return new RunicItemBook(template, item.getAmount(), nbtItem.getInteger("id"));
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
    public ItemStack generateItem() {
        ItemStack item = super.generateItem();
        if (this.getDisplayableItem().getMaterial() == Material.WRITTEN_BOOK) {
            BookMeta bookMeta = (BookMeta) item.getItemMeta();
            bookMeta.setGeneration(BookMeta.Generation.ORIGINAL);
            if (this.author != null) bookMeta.setAuthor(author);
            bookMeta.setTitle(this.displayableItem.getDisplayName());
            bookMeta.setPages(this.pages);
            item.setItemMeta(bookMeta);
        }
        return item;
    }

    @Override
    protected List<String> generateLore() {
        return lore;
    }

}
