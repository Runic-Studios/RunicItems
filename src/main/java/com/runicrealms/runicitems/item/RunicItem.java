package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class RunicItem {

    protected Long id;

    protected ItemStack currentItem; // ItemStack that we are currently displaying to the player

    protected final DisplayableItem displayableItem; // Base ItemStack information that we get from the template
    protected final String templateId; // Template ID
    protected final List<RunicItemTag> tags; // List of tags (soulbound, untradeable, etc.)
    protected final Map<String, Object> data;

    protected int count;

    protected List<ItemLoreSection> loreSections = new ArrayList<ItemLoreSection>();

    // TODO - initialize id and itemOwner
    public RunicItem(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data, int count, long id, Callable<ItemLoreSection[]> loreSectionGenerator) {
        this.templateId = templateId;
        this.displayableItem = displayableItem;
        this.tags = tags;
        this.data = data;
        this.count = count;
        this.id = id;
        this.currentItem = this.displayableItem.generateItem();
        try {
            ItemLoreSection[] loreSections = loreSectionGenerator.call();
            if (loreSections != null && loreSections.length > 0) {
                for (ItemLoreSection section : loreSections) {
                    if (section != null) {
                        this.loreSections.add(section);
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        ItemMeta meta = this.currentItem.getItemMeta();
        List<String> lore = new ArrayList<String>();
        for (ItemLoreSection section : this.loreSections) {
            lore.addAll(section.getLore());
            if (!section.isEmpty()) {
                lore.add("");
            }
        }
        for (RunicItemTag tag : this.tags) {
            lore.add(tag.getDisplay());
        }
        meta.setLore(lore);
        this.currentItem.setItemMeta(meta);
        // TODO finish generating item, add data, count
    }

    public ItemStack getCurrentItem() {
        return this.currentItem;
    }

    public ItemStack regenerateCurrentItem() {
        // TODO generate item, add data, count
        return null;
    }

    public DisplayableItem getDisplayableItem() {
        return this.displayableItem;
    }

    public String getTemplateId() {
        return this.templateId;
    }

    public List<RunicItemTag> getTags() {
        return this.tags;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) { // Requires regenerating the item to take effect!
        this.count = count;
    }

    public Map<String, Object> getData() {
        return this.data;
    }

    public void addToData(MongoDataSection section) {
        section.set("template-id", this.templateId);
        section.set("count", this.count);
        int count = 0;
        for (RunicItemTag tag : this.tags) {
            section.set("tags." + count, tag.getIdentifier());
            count++;
        }
        for (String dataKey : this.data.keySet()) {
            section.set("data." + dataKey, this.data.get(dataKey));
        }
        this.addSpecificItemToData(section);
    }

    protected abstract void addSpecificItemToData(Data section);

    public Long getId() {
        return this.id;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public boolean hasId() {
        return this.id != null;
    }

}
