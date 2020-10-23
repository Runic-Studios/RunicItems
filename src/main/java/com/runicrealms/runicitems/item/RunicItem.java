package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class RunicItem {

    protected ItemStack currentItem; // ItemStack that we are currently displaying to the player

    protected DisplayableItem displayableItem; // Base ItemStack information that we get from the template
    protected String id; // Template ID
    protected List<RunicItemTag> tags; // List of tags (soulbound, untradeable, etc.)

    protected List<ItemLoreSection> loreSections = new ArrayList<ItemLoreSection>();

    public RunicItem(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Callable<ItemLoreSection[]> loreSectionGenerator) {
        this.id = id;
        this.displayableItem = displayableItem;
        this.tags = tags;
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
        // TODO finish generating item
    }

    public ItemStack getCurrentItem() {
        return this.currentItem;
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

}
