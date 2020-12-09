package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class RunicItem {

    // TODO - add anti-dupe: unique-id, owner, inventory

    protected ItemStack currentItem; // ItemStack that we are currently displaying to the player

    protected DisplayableItem displayableItem; // Base ItemStack information that we get from the template
    protected String id; // Template ID
    protected List<RunicItemTag> tags; // List of tags (soulbound, untradeable, etc.)
    protected Map<String, Object> data;

    protected int count;

    protected List<ItemLoreSection> loreSections = new ArrayList<ItemLoreSection>();

    public RunicItem(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data, int count, Callable<ItemLoreSection[]> loreSectionGenerator) {
        this.id = id;
        this.displayableItem = displayableItem;
        this.tags = tags;
        this.data = data;
        this.count = count;
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
        } catch (Exception e) {
            e.printStackTrace();
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

    public void generateCurrentItem() {
        // TODO generate item, add data, count
    }

    public DisplayableItem getDisplayableItem() {
        return this.displayableItem;
    }

    public String getId() {
        return this.id;
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
        section.set("id", this.id);
        section.set("count", this.count);
    }

    protected abstract void addSpecificItemToData(Data section);

}
