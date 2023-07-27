package com.runicrealms.plugin.runicitems.api;

import com.runicrealms.plugin.runicitems.item.RunicItem;
import com.runicrealms.plugin.runicitems.util.NBTUtil;
import com.runicrealms.plugin.runicitems.ItemManager;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides methods for handling inventories and click events using our custom items system
 *
 * @author Skyfallin
 */
public interface InventoryAPI {

    default void combineItemStacks(Player player, InventoryClickEvent event, ItemStack currentItem, Inventory targetInventory) {
        combineItemStacks(player, event, currentItem, targetInventory, 0, targetInventory.getSize());
    }

    /**
     * Attempts to replicate shift-click logic from vanilla MC accurately with RunicItems
     *
     * @param player          who shift-clicked
     * @param currentItem     the current item in the inventory
     * @param targetInventory the inventory the items will be moved to
     * @param loopStart       start of slot to iterate over, used to handle hotbar + custom inventories separately
     * @param loopEnd         end of loop, used to stop early when dealing with hotbars
     */
    default void combineItemStacks(Player player, InventoryClickEvent event, ItemStack currentItem, Inventory targetInventory, int loopStart, int loopEnd) {
        if (event.getCurrentItem() == null) return;
        int amountLeft = currentItem.getAmount();
        for (int i = loopStart; i < loopEnd; i++) {
            ItemStack item = targetInventory.getContents()[i];
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getAmount() == item.getMaxStackSize()) continue;
            if (!NBTUtil.isNBTSimilar(item, currentItem, false, false)) continue;
            // Check if the current item can fit into the item stack
            if (item.getAmount() + amountLeft <= item.getMaxStackSize()) {
                // If it can fit, add the amount to the item stack and remove the current item from the clicked inventory
                item.setAmount(item.getAmount() + amountLeft);
                // If the entire stack was moved, remove the current item
                if (amountLeft == currentItem.getAmount()) {
                    event.setCurrentItem(null); // IMPORTANT: Removes ALL INSTANCES of the stack in the player's inventory
                } else {
                    // If not all the stack was moved, reduce the amount of the current item
                    event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - amountLeft);
                }
                event.setCancelled(true);
                // Set amount left to 0 and break out of the loop
                amountLeft = 0;
                break;
            } else {
                // If it can't fit, calculate the amount that can be added to the item stack
                int addedAmount = item.getMaxStackSize() - item.getAmount();
                // Add the amount that can be added to the item stack
                item.setAmount(item.getMaxStackSize());
                // Subtract the added amount from the amount left
                amountLeft -= addedAmount;
                // Reduce the amount of the current item
                event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - addedAmount);
            }
        }
        // If there's still some amount left, set the amount of the current item to the amount left
        if (amountLeft > 0) {
            event.getCurrentItem().setAmount(amountLeft);
        }
        // Update the player's inventory to reflect the changes
        player.updateInventory();
    }

    /**
     * A method used to clear an inventory of all items that match the provided template
     *
     * @param inventory        the inventory to wipe
     * @param amount           the amount to remove
     * @param template         the template
     * @param sender           the user who initiated the clear inventory
     * @param ignoreItemStacks if an invalid item is found, if the wipe should be stopped
     */
    default void clearInventory(@NotNull Inventory inventory, int amount, @Nullable RunicItemTemplate template, @Nullable CommandSender sender, boolean ignoreItemStacks) {
        int amountRemoved = 0;
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && contents[i].getType() != Material.AIR) {
                if (amount == -1 || amountRemoved < amount) {
                    RunicItem item = ItemManager.getRunicItemFromItemStack(contents[i]);
                    if (item == null) {
                        if (ignoreItemStacks) {
                            continue;
                        }
                        if (sender != null) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&dError removing items!"));
                        }
                        return;
                    }
                    boolean removeItem = false;
                    if (template == null) {
                        removeItem = true;
                    } else if (item.getTemplateId().equalsIgnoreCase(template.getId())) {
                        removeItem = true;
                    }
                    if (removeItem) {
                        if (contents[i].getAmount() <= amount - amountRemoved || amount == -1) {
                            amountRemoved += contents[i].getAmount();
                            inventory.setItem(i, new ItemStack(Material.AIR));
                        } else {
                            amountRemoved += amount - amountRemoved;
                            inventory.getItem(i).setAmount(inventory.getItem(i).getAmount() - (amount - amountRemoved));
                        }
                    }
                }
            }
        }
    }

    /**
     * A method used to clear an inventory of all items that match the provided template
     *
     * @param inventory the inventory to wipe
     * @param amount    the amount to remove
     * @param template  the template
     * @param sender    the user who initiated the clear inventory
     */
    default void clearInventory(@NotNull Inventory inventory, int amount, @Nullable RunicItemTemplate template, @Nullable CommandSender sender) {
        this.clearInventory(inventory, amount, template, sender, false);
    }

    /**
     * A method used to wipe all copies of a given item template from an inventory
     *
     * @param inventory        the inventory to wipe
     * @param template         the template
     * @param sender           the user who initiated the clear inventory
     * @param ignoreItemStacks if an invalid item is found, if the wipe should be stopped
     */
    default void clearInventory(@NotNull Inventory inventory, @Nullable RunicItemTemplate template, @Nullable CommandSender sender, boolean ignoreItemStacks) {
        this.clearInventory(inventory, -1, template, sender, ignoreItemStacks);
    }

    /**
     * A method used to wipe all copies of a given item template from an inventory
     *
     * @param inventory the inventory to wipe
     * @param template  the template
     * @param sender    the user who initiated the clear inventory
     */
    default void clearInventory(@NotNull Inventory inventory, @Nullable RunicItemTemplate template, @Nullable CommandSender sender) {
        this.clearInventory(inventory, -1, template, sender, false);
    }
}