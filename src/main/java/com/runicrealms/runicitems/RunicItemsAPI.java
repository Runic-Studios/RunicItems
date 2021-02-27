package com.runicrealms.runicitems;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.config.ItemLoader;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.stats.RunicArtifactAbility;
import com.runicrealms.runicitems.player.AddedPlayerStats;
import com.runicrealms.runicitems.player.PlayerStatHolder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RunicItemsAPI {

    /**
     * Gets the JavaPlugin subclass for this plugin
     * @return RunicItems
     */
    public static RunicItems getPlugin() {
        return RunicItems.getInstance();
    }

    /**
     * Generates a RunicItem from a template ID
     * You can get an ItemStack from this by using RunicItem#generateItem
     * @param templateId - ID of the item template to generate from
     * @return RunicItem
     */
    public static RunicItem generateItemFromTemplate(String templateId) {
        return TemplateManager.generateItemFromTemplateId(templateId);
    }

    /**
     * Generates a RunicItem from a template ID
     * You can get an ItemStack from this by using RunicItem#generateItem
     * @param templateId - ID of the item template to generate from
     * @param count - amount for the item
     * @return RunicItem
     */
    public static RunicItem generateItemFromTemplate(String templateId, int count) {
        return TemplateManager.generateItemFromTemplateId(templateId, count);
    }

    /**
     * Serializes inventory and adds it to a Data section
     * WARNING: This MUST NOT be called on the main thread!
     * @param inventory - Inventory to save
     * @param data - Data to save to
     */
    public static void saveInventoryToData(Inventory inventory, Data data) {
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                RunicItem runicItem = ItemManager.getRunicItemFromItemStack(contents[i]);
                if (runicItem != null) {
                    runicItem.addToData(data, i + "");
                }
            }
        }
        data.save();
    }

    /**
     * Deserializes an inventory stored in a Data section and applies it to an existing inventory
     * @param inventory - Inventory to modify
     * @param data - Data to read from
     */
    public static void applyDataToInventory(Inventory inventory, Data data) {
        for (String key : data.getKeys()) {
            inventory.setItem(Integer.parseInt(key), ItemLoader.loadItem(data.getSection(key), DupeManager.getNextItemId()).generateItem());
        }
    }

    /**
     * Creates a RunicItem from an ItemStack
     * WARNING: Modifying the RunicItem WILL NOT change the ItemStack! For that you must use RunicItem#generateItem.
     * WARNING: This method is NOT very efficient (running many NBT checks), if run frequently it should not be on the main thread.
     * @param itemStack - ItemStack to read from
     * @return RunicItem
     */
    public static RunicItem getRunicItemFromItemStack(ItemStack itemStack) {
        return ItemManager.getRunicItemFromItemStack(itemStack);
    }

    /**
     * Gets a RunicArtifactAbility from it's ID in abilities.yml
     * This plugin does not handle the actual abilities, but does have a function to represent them
     * @param id - ID of the ability in abilities.yml
     * @return RunicArtifactAbility
     */
    public static RunicArtifactAbility getAbilityFromId(String id) {
        return AbilityManager.getAbilityFromId(id);
    }

    /**
     * Gets the (cached) added stats from a players armor, weapon and offhand.
     * @param player - Player to check
     * @return AddedPlayerStats
     */
    public static AddedPlayerStats getAddedPlayerStats(Player player) {
        return PlayerManager.getCachedPlayerStats().get(player).getTotalStats();
    }

    /**
     * Gets a list of the cached items (armor, weapon and offhand) that a player is using
     * @param player - Player to check
     * @return PlayerStatHolder
     */
    public static PlayerStatHolder getCachedPlayerItems(Player player) {
        return PlayerManager.getCachedPlayerStats().get(player);
    }

}
