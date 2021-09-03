package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemDynamicTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * This is the class that actually gets the RunicItemDynamic from template
 * RunicItemDynamic is a type of RunicItemGeneric that needs to dynamically update its NBT/Lore
 * during runtime.
 *
 * This includes gold pouches, gathering tools, etc.
 */
public class RunicItemDynamic extends RunicItemGeneric {

    private static final String DYNAMIC_FIELD_STRING = "dynamic";
    private int dynamicField;

    public RunicItemDynamic(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags,
                            Map<String, String> data, int count, long id, Map<ClickTrigger, String> triggers,
                            List<String> lore, int dynamicField) {
        super(templateId, displayableItem, tags, data, count, id, triggers, lore);
        this.dynamicField = dynamicField;
    }

    /**
     * This gets called when a character loads into the server, and grabs the item data from the database
     *
     * @param template the cached YAML template to load basic item info from
     * @param count the number of item from database
     * @param id the template id from the database
     * @param dynamicField an integer that can store a dynamic value (e.g. current coins for gold pouch)
     */
    public RunicItemDynamic(RunicItemDynamicTemplate template, int count, long id, int dynamicField) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getTriggers(), template.getLore(),
                dynamicField
        );
    }

    public int getDynamicField() {
        return dynamicField;
    }

    public void setDynamicField(int dynamicField) {
        this.dynamicField = dynamicField;
    }

    /*
    Store dynamic data that changes during runtime
     */
    @Override
    public void addToData(Data section, String root) {
        super.addToData(section, root);
        section.set(ItemManager.getInventoryPath() + "." + root + "." + DYNAMIC_FIELD_STRING, this.getDynamicField());
    }

    @Override
    public ItemStack generateItem() {
        ItemStack item = super.generateItem();
        ItemMeta meta = item.getItemMeta();
        String dynamicLore = this.getData().get("dynamicLore") != null ? this.getData().get("dynamicLore") : "";
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

    /**
     * Implements custom dynamic functionality by modifying our dynamic field from NBT
     *
     * @param item the dynamic ItemStack
     * @return a RunicItemDynamic object with the current stored custom field value
     */
    public static RunicItemDynamic getFromItemStack(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemDynamicTemplate)) throw new IllegalArgumentException("ItemStack is not a dynamic item!");
        RunicItemDynamicTemplate template = (RunicItemDynamicTemplate) uncastedTemplate;
        return new RunicItemDynamic(template, item.getAmount(), nbtItem.getInteger("id"), nbtItem.getInteger(DYNAMIC_FIELD_STRING));
    }

    public static String getDynamicFieldString() {
        return DYNAMIC_FIELD_STRING;
    }
}
