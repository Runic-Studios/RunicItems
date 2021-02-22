package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemBookTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.ItemNbtUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RunicItemBook extends RunicItem {

    private final List<String> lore;
    private final String author;
    private final Collection<String> pages;

    public RunicItemBook(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                         List<String> lore, String author, Collection<String> pages) {
        super(templateId, displayableItem, tags, data, count, id, () -> new ItemLoreSection[] {new ItemLoreSection(lore)});
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
            bookMeta.setAuthor(author);
            for (String page : pages) {
                bookMeta.addPage(page);
            }
        }
        return item;
    }

    public static RunicItemBook getFromItemStack(ItemStack item) {
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(ItemNbtUtils.getNbtString(item, "template-id"));
        if (!(uncastedTemplate instanceof RunicItemBookTemplate)) throw new IllegalArgumentException("ItemStack is not a book item!");
        RunicItemBookTemplate template = (RunicItemBookTemplate) uncastedTemplate;
        return new RunicItemBook(template, item.getAmount(), ItemNbtUtils.getNbtInteger(item, "id"));
    }

}
