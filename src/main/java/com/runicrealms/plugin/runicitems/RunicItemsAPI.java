package com.runicrealms.plugin.runicitems;

import com.runicrealms.plugin.runicitems.api.AntiDupeInventoryHandler;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import com.runicrealms.plugin.runicitems.player.AddedPlayerStats;
import com.runicrealms.plugin.runicitems.player.PlayerStatHolder;
import com.runicrealms.plugin.runicitems.util.NBTUtil;
import com.runicrealms.plugin.runicitems.item.stats.RunicArtifactAbility;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RunicItemsAPI {

    /**
     * Gets the JavaPlugin subclass for this plugin
     *
     * @return RunicItems
     */
    public static RunicItems getPlugin() {
        return RunicItems.getInstance();
    }

    /**
     * Check whether the item contains a 'blocked' tag, for use in preventing trades and/or storage
     *
     * @param runicItem the item attempting to be traded/stored/etc.
     * @return true if it contains any of the following tags
     */
    public static boolean containsBlockedTag(RunicItem runicItem) {
        return runicItem.getTags().contains(RunicItemTag.QUEST_ITEM)
                || runicItem.getTags().contains(RunicItemTag.SOULBOUND)
                || runicItem.getTags().contains(RunicItemTag.UNTRADEABLE);
    }

    /**
     * Generates a RunicItem from a template ID
     * You can get an ItemStack from this by using RunicItem#generateItem
     *
     * @param templateId - ID of the item template to generate from
     * @return RunicItem
     */
    public static RunicItem generateItemFromTemplate(String templateId) {
        return TemplateManager.generateItemFromTemplateId(templateId);
    }

    /**
     * Generates a RunicItem from a template ID
     * You can get an ItemStack from this by using RunicItem#generateItem
     *
     * @param templateId - ID of the item template to generate from
     * @param count      - amount for the item
     * @return RunicItem
     */
    public static RunicItem generateItemFromTemplate(String templateId, int count) {
        return TemplateManager.generateItemFromTemplateId(templateId, count);
    }

    /**
     * Generates a random runic item (weapon or armor) in the specified level range
     *
     * @param minimumLevel lower level bound (inclusive)
     * @param maximumLevel upper level bound (inclusive)
     * @param count        of items
     * @return a RunicItem object
     */
    public static RunicItem generateItemInRange(int minimumLevel, int maximumLevel, int count) {
        RunicItemTemplate template = LootManager.getRandomItemInRange(minimumLevel, maximumLevel);
        return template.generateItem(count, DupeManager.getNextItemId(), null, null);
    }

    /**
     * Creates a RunicItem from an ItemStack
     * WARNING: Modifying the RunicItem WILL NOT change the ItemStack! For that you must use RunicItem#generateItem.
     * WARNING: This method is NOT very efficient (running many NBT checks), if runs frequently it should not be on the main thread.
     *
     * @param itemStack - ItemStack to read from
     * @return RunicItem
     */
    public static RunicItem getRunicItemFromItemStack(ItemStack itemStack) {
        return ItemManager.getRunicItemFromItemStack(itemStack);
    }

    /**
     * Gets a RunicArtifactAbility from it's ID in abilities.yml
     * This plugin does not handle the actual abilities, but does have a function to represent them
     *
     * @param id - ID of the ability in abilities.yml
     * @return RunicArtifactAbility
     */
    public static RunicArtifactAbility getAbilityFromId(String id) {
        return AbilityManager.getAbilityFromId(id);
    }

    /**
     * Gets the (cached) added stats from a players armor, weapon and offhand.
     *
     * @param uuid - UUID of player to check
     * @return AddedPlayerStats
     */
    public static AddedPlayerStats getAddedPlayerStats(UUID uuid) {
        return PlayerManager.getCachedPlayerStats().get(uuid).getTotalStats();
    }

    /**
     * Gets a list of the cached items (armor, weapon and offhand) that a player is using
     *
     * @param uuid - UUID of player to check
     * @return PlayerStatHolder
     */
    public static PlayerStatHolder getCachedPlayerItems(UUID uuid) {
        return PlayerManager.getCachedPlayerStats().get(uuid);
    }

    /**
     * Checks if two ItemStack generated from runic items are of the same template.
     * This ignores anti-dupe NBT tags like last-count and id. Comparing with ItemStack#isSimilar will not work,
     * this is a replacement for that method.
     *
     * @param firstRunicItemStack  First item stack
     * @param secondRunicItemStack Second item stack
     * @return If the item stacks have the same template
     */
    public static boolean isRunicItemSimilar(ItemStack firstRunicItemStack, ItemStack secondRunicItemStack) {
        return NBTUtil.isNBTSimilar(firstRunicItemStack, secondRunicItemStack, false, false);
    }

    /**
     * Adds an ItemStack generated by a RunicItem into an inventory,
     * <b>while stacking the item with other stacks properly.</b>
     * WARNING: only works on ItemStacks generated from runic items!
     *
     * @param inventory   Inventory to add to
     * @param itemStack   ItemStack to add
     * @param assignNewId Should we assign a new dupe ID to the item
     * @return list of overflow items
     */
    public static HashMap<Integer, ItemStack> addItem(Inventory inventory, ItemStack itemStack, boolean assignNewId) {

        // Exit if item is null/air
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;

        // Add any potential missing NBT tags
        DupeManager.checkMissingDupeNBT(itemStack);

        // Assign new Dupe ID
        if (assignNewId) DupeManager.assignNewDupeId(itemStack);

        // While iterating over inventory, keep track of how many of this item we have left to add
        int amountLeft = itemStack.getAmount();
        // Contents of this inventory
        ItemStack[] contents = inventory.getContents();
        // List of inventory slots that are being updated, need to receive new IDs
        List<Integer> slotsReceivingNewIds = new LinkedList<>();

        // Loop over contents
        for (int i = 0; i < contents.length; i++) {

            ItemStack item = contents[i];

            if (amountLeft > 0) {

                // Exit if slot empty/we can't stack
                if (item == null || item.getType() == Material.AIR) continue;
                if (item.getAmount() == item.getMaxStackSize()) continue;

                // Check if the iterated item and our item have the same template
                if (NBTUtil.isNBTSimilar(itemStack, item, false, false)) {

                    // Clone item
                    ItemStack itemToAdd = item.clone();

                    // Depending on whether or not we can fit the rest of our items into the stack or not...
                    if (item.getAmount() + amountLeft <= item.getMaxStackSize()) {
                        // If we can fit the entire amount into this stack, exit
                        itemToAdd.setAmount(amountLeft);
                        amountLeft = 0;

                    } else if (item.getAmount() + amountLeft > item.getMaxStackSize()) {
                        // If we can't fit (we have overflow), continue
                        int amountAdded = item.getMaxStackSize() - item.getAmount();
                        itemToAdd.setAmount(amountAdded);
                        amountLeft -= amountAdded;

                    }

                    // This item has been updated, add to list of slots receiving new item IDs
                    slotsReceivingNewIds.add(i);

                    // Add item
                    inventory.addItem(itemToAdd);

                }

            } else break;

        }

        // reassign some IDs for anti dupe
        for (int slot : slotsReceivingNewIds) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                DupeManager.assignNewDupeId(item);
            }
        }

        // if we still have items unstacked, add normally to inventory
        if (amountLeft > 0) {
            itemStack.setAmount(amountLeft);
            return inventory.addItem(itemStack);
        }

        return new HashMap<>(); // return an empty hashmap

    }

    /**
     * Adds an ItemStack generated by a RunicItem into an inventory,
     * <b>while stacking the item with other stacks properly.</b>
     * WARNING: only works on ItemStacks generated from runic items!
     * <p>
     * This method will drop overflow items on the ground (at location)!
     *
     * @param inventory   Inventory to add to
     * @param itemStack   ItemStack to add
     * @param location    the location to drop the items
     * @param assignNewId Should we assign a new dupe ID to the item
     */
    public static void addItem(Inventory inventory, ItemStack itemStack, Location location, boolean assignNewId) {
        addItem(inventory, itemStack, assignNewId).forEach((slot, leftOver) -> Objects.requireNonNull(location.getWorld()).dropItem(location, leftOver));
    }


    /**
     * Adds an ItemStack generated by a RunicItem into an inventory,
     * <b>while stacking the item with other stacks properly.</b>
     * WARNING: only works on ItemStacks generated from runic items!
     *
     * @param inventory Inventory to add to
     * @param itemStack ItemStack to add
     * @return list of overflow items
     */
    public static HashMap<Integer, ItemStack> addItem(Inventory inventory, ItemStack itemStack) {
        return addItem(inventory, itemStack, false);
    }

    /**
     * Adds an ItemStack generated by a RunicItem into an inventory,
     * <b>while stacking the item with other stacks properly.</b>
     * WARNING: only works on ItemStacks generated from runic items!
     * <p>
     * This method will also drop overflow items on the ground (at location)!
     *
     * @param inventory Inventory to add to
     * @param itemStack ItemStack to add
     * @param location  the location to drop the items
     */
    public static void addItem(Inventory inventory, ItemStack itemStack, Location location) {
        addItem(inventory, itemStack, false).forEach((slot, leftOver) -> Objects.requireNonNull(location.getWorld()).dropItem(location, leftOver));
    }

    /**
     * Gets the template of an ItemStack.
     * Returns null if the item stack doesn't have a template.
     *
     * @param itemStack ItemStack to check
     * @return Template
     */
    public static RunicItemTemplate getItemStackTemplate(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasNBTData()) return null;
        if (!nbtItem.hasKey("template-id")) return null;
        return TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
    }

    /**
     * Registers a handler for plugins that aim to prevent dupes when the player has a specific inventory open.
     *
     * @param handler Handler
     */
    public static void registerAntiDupeInventoryHandler(AntiDupeInventoryHandler handler) {
        DupeManager.registerAntiDupeInventoryHandler(handler);
    }

    /**
     * Gets a collection of all item templates that have been loaded.
     *
     * @return Template collection
     */
    public static Collection<RunicItemTemplate> getTemplates() {
        return TemplateManager.getTemplates().values();
    }

    /**
     * Checks if a given string is a valid ID of a loaded template
     *
     * @param templateId Template ID (String)
     * @return Is a template?
     */
    public static boolean isTemplate(String templateId) {
        return TemplateManager.getTemplateFromId(templateId) != null;
    }

    /**
     * Gets a RunicItemTemplate from its given ID.
     *
     * @param templateId Template ID
     * @return RunicItemTemplate if found, null if not
     */
    public static @Nullable RunicItemTemplate getTemplate(String templateId) {
        return TemplateManager.getTemplateFromId(templateId);
    }

}
