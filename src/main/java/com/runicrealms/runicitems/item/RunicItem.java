package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.util.DataUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class RunicItem {

    protected Long id;

    protected final DisplayableItem displayableItem; // Base ItemStack information that we get from the template
    protected final String templateId; // Template ID
    protected Callable<ItemLoreSection[]> loreSectionGenerator;

    protected final List<RunicItemTag> tags; // List of tags (soulbound, untradeable, etc.)
    protected final Map<String, String> data;

    protected int count;

    protected List<ItemLoreSection> loreSections = new ArrayList<>();

    public RunicItem(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id, Callable<ItemLoreSection[]> loreSectionGenerator) {
        this.templateId = templateId;
        this.displayableItem = displayableItem;
        this.tags = tags;
        this.data = data;
        this.count = count;
        this.id = id;
        this.loreSectionGenerator = loreSectionGenerator;
    }

    public ItemStack generateItem() {
        ItemStack item = this.displayableItem.generateItem(this.count);
        try {
            ItemLoreSection[] loreSections = this.loreSectionGenerator.call();
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
        ItemMeta meta = item.getItemMeta() != null ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        meta.setDisplayName(ChatColor.WHITE + this.getDisplayableItem().getDisplayName());
        List<String> lore = new ArrayList<>();
        for (ItemLoreSection section : this.loreSections) {
            for (String s : section.getLore()) {
                lore.add(ColorUtil.format(s));
            }
            if (!section.isEmpty() && !section.equals(this.loreSections.get(this.loreSections.size() - 1))) { // no space for last section
                lore.add("");
            }
        }
        for (RunicItemTag tag : this.tags) {
            lore.add(tag.getDisplay());
        }
        // set other flags
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        if (item.getType() == Material.POTION) {
            ((PotionMeta) meta).setColor(DataUtil.getColorFromData(this));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        NBTItem nbtItem = new NBTItem(item, true);
        nbtItem.setLong("id", this.id);
        nbtItem.setString("template-id", this.templateId);
        nbtItem.setInteger("last-count", this.count);
        for (RunicItemTag tag : this.tags) {
            nbtItem.setByte(tag.getIdentifier(), (byte) 1);
        }
        for (String dataKey : this.data.keySet()) {
            nbtItem.setString("data-" + dataKey, this.data.get(dataKey));
        }
        return item;
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

    public Map<String, String> getData() {
        return this.data;
    }

    public void addToData(Data section, String root) {
        section.set(ItemManager.getInventoryPath() + "." + root + ".template-id", this.templateId);
        section.set(ItemManager.getInventoryPath() + "." + root + ".count", this.count);
        int count = 0;
        for (RunicItemTag tag : this.tags) {
            section.set(ItemManager.getInventoryPath() + "." + root + ".tags." + count, tag.getIdentifier());
            count++;
        }
        // This is not needed as item data is static for the template it comes from. Use RunicItemDynamic instead.
        /*for (String dataKey : this.data.keySet()) {
            section.set(ItemManager.getInventoryPath() + "." + root + ".data." + dataKey, this.data.get(dataKey));
        }*/
    }

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
