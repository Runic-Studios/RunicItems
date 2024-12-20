package com.runicrealms.plugin.runicitems.item;

import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;
import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;
import com.runicrealms.plugin.runicitems.util.DataUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class RunicItem {
    private static final AttributeModifier attributeModifier = new AttributeModifier("generic.attackSpeed", Integer.MAX_VALUE, AttributeModifier.Operation.ADD_NUMBER);

    protected DisplayableItem displayableItem; // Base ItemStack information that we get from the template
    protected String templateId; // Template ID
    protected List<RunicItemTag> tags; // List of tags (soulbound, untradeable, etc.)
    protected Map<String, String> data;
    protected Long id;
    protected int count;
    // If this is an icon to be used in a menu
    protected boolean isMenuDisplay = false;

    public RunicItem() {

    }

    public RunicItem(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id) {
        this.templateId = templateId;
        this.displayableItem = displayableItem;
        this.tags = tags;
        this.data = data;
        this.count = count;
        this.id = id;
    }

    /**
     * @return a map to store in Redis
     */
    public Map<String, String> addToRedis() {
        Map<String, String> jedisDataMap = new HashMap<>();
        jedisDataMap.put("template-id", this.templateId);
        jedisDataMap.put("count", String.valueOf(this.count));
        int count = 0;
        // Data
        for (String dataKey : this.data.keySet()) {
            jedisDataMap.put(dataKey, this.data.get(dataKey));
        }
        // Tags
        for (RunicItemTag tag : this.tags) {
            jedisDataMap.put("tags:" + count, tag.getIdentifier());
            count++;
        }
        return jedisDataMap;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public ItemStack generateGUIItem() {
        ItemStack item = generateItem();
        NBTItem nbtItem = new NBTItem(item, true);
        nbtItem.setBoolean("isRI", true);
        nbtItem.removeKey("id");
        return item;
    }

    public ItemStack generateItem() {
        ItemStack item = this.displayableItem.generateItem(this.count);

        ItemMeta meta = item.getItemMeta() != null ? item.getItemMeta() : Bukkit.getItemFactory().getItemMeta(item.getType());
        meta.setDisplayName(ChatColor.WHITE + this.getDisplayableItem().getDisplayName());
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, attributeModifier);
        List<String> generatedLore = generateLore();
        List<String> processedLore = new LinkedList<>();
        for (String line : generatedLore) {
            processedLore.add(ColorUtil.format(line.replace("<3", Stat.HEALTH_ICON)));
        }

        if (this.tags.size() >= 1) {
            processedLore.add("");
            for (RunicItemTag tag : this.tags) {
                if (tag.getDisplay() == null) {
                    continue;
                }

                processedLore.add(tag.getDisplay());
            }
        }

        // set other flags
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        if (item.getType() == Material.POTION) {
            ((PotionMeta) meta).setColor(DataUtil.getColorFromData(this));
        }
        meta.setLore(processedLore);
        item.setItemMeta(meta);
        NBTItem nbtItem = new NBTItem(item, true);
        nbtItem.setLong("id", this.id);
        nbtItem.setString("template-id", this.templateId);
        nbtItem.setInteger("last-count", this.count);
        nbtItem.setBoolean("isRI", true);
        for (RunicItemTag tag : this.tags) {
            nbtItem.setByte(tag.getIdentifier(), (byte) 1);
        }
        for (String dataKey : this.data.keySet()) {
            nbtItem.setString("data-" + dataKey, this.data.get(dataKey));
        }
        return item;
    }

    protected abstract List<String> generateLore();

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) { // Requires regenerating the item to take effect!
        this.count = count;
    }

    public Map<String, String> getData() {
        return this.data;
    }

    public DisplayableItem getDisplayableItem() {
        return this.displayableItem;
    }

    public Long getId() {
        return this.id;
    }

    public List<RunicItemTag> getTags() {
        return this.tags;
    }

    public String getTemplateId() {
        return this.templateId;
    }

    public boolean hasId() {
        return this.id != null;
    }

    public boolean isMenuDisplay() {
        return this.isMenuDisplay;
    }

    public void setIsMenuDisplay(boolean isMenuDisplay) {
        this.isMenuDisplay = isMenuDisplay;
    }

    /**
     * Method to write this item to a mongo document
     *
     * @param source   the item to write
     * @param document the document to write to
     * @return the document with modified fields
     */
    public Document writeToDocument(RunicItem source, Document document) {
        document.put("template-id", source.getTemplateId());
        document.put("count", source.getCount());
        Map<String, String> tagsMap = new HashMap<>();
        int tagCount = 0;
        for (RunicItemTag tag : source.getTags()) {
            tagsMap.put(String.valueOf(tagCount), tag.getIdentifier());
            tagCount++;
        }
        document.put("tags", tagsMap);
        return document;
    }

}
