package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.database.Data;
import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemBagTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * This is the class that actually gets the RunicItem from template
 */
public class RunicItemBag extends RunicItemGeneric {

    private int coins;

    public RunicItemBag(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags,
                        Map<String, String> data, int count, long id, Map<ClickTrigger, String> triggers,
                        List<String> lore, int coins) {
        super(templateId, displayableItem, tags, data, count, id, triggers, lore);
        this.coins = coins;
    }

    /**
     * This gets called when a character loads into the server, and grabs the item data from the database
     *
     * @param template the cached YAML template to load basic item info from
     * @param count the number of item from database
     * @param id the template id from the database
     * @param coins the number of coins stored for this runic bag
     */
    public RunicItemBag(RunicItemBagTemplate template, int count, long id, int coins) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getTriggers(), template.getLore(),
                coins
        );
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    @Override
    public void addToData(Data section, String root) {
        super.addToData(section, root);
        section.set(ItemManager.getInventoryPath() + "." + root + ".coins", this.getCoins());
    }

    @Override
    public ItemStack generateItem() {
        ItemStack item = super.generateItem();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName
                (
                        this.getDisplayableItem().getDisplayName() + " " +
                                ChatColor.GREEN + ChatColor.BOLD + this.getCoins() + "c"
                ); // show current number of coins
        item.setItemMeta(meta);
        NBTItem nbtItem = new NBTItem(item, true);
        nbtItem.setInteger("coins", this.getCoins());
        return item;
    }

    /**
     * Implements custom bag functionality by modifying our coin field from NBT
     *
     * @param item the gold pouch ItemStack
     * @return a RunicItemBag object with the current stored coin value
     */
    public static RunicItemBag getFromItemStack(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemBagTemplate)) throw new IllegalArgumentException("ItemStack is not a bag item!");
        RunicItemBagTemplate template = (RunicItemBagTemplate) uncastedTemplate;
        return new RunicItemBag(template, item.getAmount(), nbtItem.getInteger("id"), nbtItem.getInteger("coins"));
    }

    /**
     * Fills our gold pouch item using gold from the player's inventory
     */
    public void fillPouch(Player player) {
        int currentAmount = this.getCoins();
        int maxAmount = Integer.parseInt(this.getData().get("maxCoins"));
        int amountToFill = maxAmount - currentAmount;
        // if player has enough coins to fill the pouch, fill it
        if (RunicCoreAPI.hasItems(player, CurrencyUtil.goldCoin(), amountToFill)) {
            ItemRemover.takeItem(player, CurrencyUtil.goldCoin(), amountToFill);
            this.setCoins(maxAmount);
            return;
        }
        // if player does not have enough coins to fill it, start filling it using the largest stack size possible
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 64);
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 48);
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 32);
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 16);
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 8);
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 4);
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 2);
        currentAmount = removeGoldStackSize(currentAmount, maxAmount, player, 1);
        this.setCoins(currentAmount);
    }

    /**
     * If a player doesn't have enough coins to fill a pouch, we manually start filling it by the largest stack possible
     *
     * @param currentAmount the amount of coins in the pouch
     * @param maxAmount the total amount of coins the pouch can hold
     * @param player to check inventory from
     * @param stackSize the amount of coins we will try to fill
     * @return the new current amount of coins in the pouch
     */
    private int removeGoldStackSize(int currentAmount, int maxAmount, Player player, int stackSize) {
        while (RunicCoreAPI.hasItems(player, CurrencyUtil.goldCoin(), stackSize) && currentAmount < maxAmount) {
            // remove it, add to pouch
            ItemRemover.takeItem(player, CurrencyUtil.goldCoin(), stackSize);
            currentAmount += stackSize;
        }
        return currentAmount;
    }

    /**
     * Empties our RunicItemBag object of its stored coins with a reference to the previous amount
     * Should be used before generateItem
     *
     * @return the previously held coins amount
     */
    public int emptyPouch() {
        int coins = this.coins;
        setCoins(0);
        return coins;
    }

}
