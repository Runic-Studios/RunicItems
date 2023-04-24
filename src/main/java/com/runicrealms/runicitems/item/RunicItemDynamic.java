package com.runicrealms.runicitems.item;

import com.destroystokyo.paper.Namespaced;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemDynamicTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is the class that actually gets the RunicItemDynamic from template
 * RunicItemDynamic is a type of RunicItemGeneric that needs to dynamically update its NBT/Lore
 * during runtime.
 * <p>
 * This includes gold pouches, gathering tools, etc.
 */
public class RunicItemDynamic extends RunicItemGeneric {

    private static final String DYNAMIC_FIELD_STRING = "dynamic";
    private static final String DYNAMIC_FIELD_INITIAL_VALUE = "dynamicFieldInitialValue";
    private int dynamicField;

    /**
     * Constructor to initialize dynamic item with dynamic field initial value equal to the template value
     */
    public RunicItemDynamic(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags,
                            Map<String, String> data, int count, long id, Map<ClickTrigger, String> triggers,
                            List<String> lore) {
        super(templateId, displayableItem, tags, data, count, id, triggers, lore);
        this.dynamicField = this.getData().get(DYNAMIC_FIELD_INITIAL_VALUE) != null ? Integer.parseInt(this.getData().get(DYNAMIC_FIELD_INITIAL_VALUE)) : 0;
    }

    /**
     * Constructor to initialize dynamic item with dynamic field initial value equal to the stored value in player's database
     *
     * @param dynamicField from the player's database info
     */
    public RunicItemDynamic(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags,
                            Map<String, String> data, int count, long id, Map<ClickTrigger, String> triggers,
                            List<String> lore, int dynamicField) {
        super(templateId, displayableItem, tags, data, count, id, triggers, lore);
        this.dynamicField = dynamicField;
    }

    /**
     * This constructor gets called when a character loads into the server, and grabs the item data from the database
     *
     * @param template     the cached YAML template to load basic item info from
     * @param count        the number of item from database
     * @param id           the template id from the database
     * @param dynamicField an integer that can store a dynamic value (e.g. current coins for gold pouch)
     */
    public RunicItemDynamic(RunicItemDynamicTemplate template, int count, long id, int dynamicField) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getTriggers(), template.getLore(),
                dynamicField
        );
    }

    /**
     * Implements custom dynamic functionality by modifying our dynamic field from NBT
     *
     * @param item the dynamic ItemStack
     * @return a RunicItemDynamic object with the current stored custom field value
     */
    public static RunicItemDynamic getFromItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemDynamicTemplate))
            throw new IllegalArgumentException("ItemStack is not a dynamic item!");
        RunicItemDynamicTemplate template = (RunicItemDynamicTemplate) uncastedTemplate;
        return new RunicItemDynamic(template, item.getAmount(), nbtItem.getInteger("id"), nbtItem.getInteger(DYNAMIC_FIELD_STRING));
    }

    public static String getDynamicFieldString() {
        return DYNAMIC_FIELD_STRING;
    }

    @Override
    public Map<String, String> addToJedis() {
        Map<String, String> jedisDataMap = super.addToJedis();
        jedisDataMap.put(DYNAMIC_FIELD_STRING, String.valueOf(this.getDynamicField()));
        return jedisDataMap;
    }

    @Override
    public ItemStack generateItem() {
        ItemStack item = super.generateItem();
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.setDestroyableKeys(destroyBlocks());
        String dynamicLore = ColorUtil.format(this.getData().get("dynamicLore") != null ? this.getData().get("dynamicLore") : "");
        if (this.getDynamicField() > 0) {
            meta.setDisplayName
                    (
                            this.getDisplayableItem().getDisplayName() + " " +
                                    ChatColor.WHITE + this.getDynamicField() + dynamicLore
                    ); // show dynamic data
        }
        item.setItemMeta(meta);
        NBTItem nbtItem = new NBTItem(item, true);
        nbtItem.setInteger(DYNAMIC_FIELD_STRING, this.getDynamicField());
        return item;
    }

    @Override
    public org.bson.Document writeToDocument(RunicItem source, org.bson.Document document) {
        document = super.writeToDocument(source, document);
        document.put(DYNAMIC_FIELD_STRING, this.getDynamicField());
        return document;
    }

    private Collection<Namespaced> destroyBlocks() {
        // Farming
        NamespacedKey wheat = NamespacedKey.minecraft("wheat");
        NamespacedKey carrot = NamespacedKey.minecraft("carrots");
        NamespacedKey potato = NamespacedKey.minecraft("potatoes");
        NamespacedKey melon = NamespacedKey.minecraft("melon");
        // Mining
        NamespacedKey iron_ore = NamespacedKey.minecraft("iron_ore");
        NamespacedKey gold_ore = NamespacedKey.minecraft("gold_ore");
        NamespacedKey redstone_ore = NamespacedKey.minecraft("redstone_ore");
        NamespacedKey lapis_ore = NamespacedKey.minecraft("lapis_ore");
        NamespacedKey diamond_ore = NamespacedKey.minecraft("diamond_ore");
        NamespacedKey emerald_ore = NamespacedKey.minecraft("emerald_ore");
        NamespacedKey nether_quartz_ore = NamespacedKey.minecraft("nether_quartz_ore");
        // Woodcutting
        NamespacedKey oak_log = NamespacedKey.minecraft("oak_wood");
        NamespacedKey spruce_log = NamespacedKey.minecraft("spruce_wood");
        NamespacedKey birch_log = NamespacedKey.minecraft("birch_wood");
        NamespacedKey acacia_log = NamespacedKey.minecraft("acacia_wood");
        NamespacedKey dark_oak_log = NamespacedKey.minecraft("dark_oak_wood");
        NamespacedKey jungle_log = NamespacedKey.minecraft("jungle_wood");
        return Arrays.asList
                (
                        wheat,
                        carrot,
                        potato,
                        melon,
                        iron_ore,
                        gold_ore,
                        redstone_ore,
                        lapis_ore,
                        diamond_ore,
                        emerald_ore,
                        nether_quartz_ore,
                        oak_log,
                        spruce_log,
                        birch_log,
                        acacia_log,
                        dark_oak_log,
                        jungle_log
                );
    }

    public int getDynamicField() {
        return dynamicField;
    }

    public void setDynamicField(int dynamicField) {
        this.dynamicField = dynamicField;
    }

    /**
     * Updates the NBT/Lore of our associated ItemStack to our current dynamic field value
     *
     * @param itemStack associated w/ our RunicItem
     * @return the ItemStack w/ updated NBT
     */
    public ItemStack updateItemStack(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        String dynamicLore = ColorUtil.format(this.getData().get("dynamicLore") != null ? this.getData().get("dynamicLore") : "");
        if (this.getDynamicField() > 0) {
            meta.setDisplayName
                    (
                            this.getDisplayableItem().getDisplayName() + " " +
                                    ChatColor.WHITE + this.getDynamicField() + dynamicLore
                    ); // show dynamic data
        }
        itemStack.setItemMeta(meta);
        NBTItem nbtItem = new NBTItem(itemStack, true);
        nbtItem.setInteger(DYNAMIC_FIELD_STRING, this.getDynamicField());
        return itemStack;
    }
}
