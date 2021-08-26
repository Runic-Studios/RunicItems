package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemBagTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * This is the class that actually gets the RunicItem from template
 */
public class RunicItemBag extends RunicItemGeneric {

    private final int coins;
    private final int maxNumberOfCoins;

    public RunicItemBag(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags,
                        Map<String, String> data, int count, long id, Map<ClickTrigger, String> triggers,
                        List<String> lore, int coins, int maxNumberOfCoins) {
        super(templateId, displayableItem, tags, data, count, id, triggers, lore);
        this.coins = coins;
        this.maxNumberOfCoins = maxNumberOfCoins;
    }

    public RunicItemBag(RunicItemBagTemplate template, int count, long id) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getTriggers(), template.getLore(),
                template.getCoins(), template.getMaxNumberOfCoins()
        );
    }

    public int getCoins() {
        return coins;
    }

    public int getMaxNumberOfCoins() {
        return maxNumberOfCoins;
    }

    @Override
    public void addToData(Data section, String root) {
        super.addToData(section, root);
        section.set(ItemManager.getInventoryPath() + "." + root + ".coins", this.getCoins());
        section.set(ItemManager.getInventoryPath() + "." + root + ".maxNumberOfCoins", this.getMaxNumberOfCoins());
    }

    @Override
    public ItemStack generateItem() {
        ItemStack item = super.generateItem();
        ItemMeta meta = item.getItemMeta();
//        meta.setDisplayName(this.getRarity().getChatColor() + this.getDisplayableItem().getDisplayName()); // apply rarity color
        item.setItemMeta(meta);
        NBTItem nbtItem = new NBTItem(item, true);
        nbtItem.setInteger("coins", this.getCoins());
        nbtItem.setInteger("maxNumberOfCoins", this.getMaxNumberOfCoins());
        return item;
    }

    /**
     * This...
     *
     * @param item
     * @return
     */
    public static RunicItemBag getFromItemStack(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemBagTemplate)) throw new IllegalArgumentException("ItemStack is not a bag item!");
        RunicItemBagTemplate template = (RunicItemBagTemplate) uncastedTemplate;
        return new RunicItemBag(template, item.getAmount(), nbtItem.getInteger("id"));
    }
}
