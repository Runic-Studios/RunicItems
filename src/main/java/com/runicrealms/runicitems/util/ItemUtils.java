package com.runicrealms.runicitems.util;

import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ItemUtils {

    /**
     * Removes items from a player's inventory, typically when they purchase an item in a shop. Handles custom
     * NBT through ItemStacks generated by RunicItems
     *
     * @param player    to remove items from
     * @param itemStack item to remove
     * @param amount    amount of item to remove
     */
    public static void takeItem(Player player, ItemStack itemStack, int amount) {
        int to_take = amount;
        for (ItemStack player_item : player.getInventory().getContents()) {
            if (player_item != null) {
                if (RunicItemsAPI.isRunicItemSimilar(itemStack, player_item)) {
                    int take_next = Math.min(to_take, player_item.getAmount());
                    remove(player, player_item, take_next);
                    to_take -= take_next;
                    if (to_take <= 0) { //Reached amount. Can stop!
                        break;
                    }
                }
            }
        }
    }

    /**
     * Removes items from a player's inventory based on material, typically for profession stations
     * Outdated and will be removed with profession rework
     *
     * @param player   to remove items from
     * @param material of item to match
     * @param amount   amount of item to remove
     */
    public static void takeItem(Player player, Material material, int amount) {
        int to_take = amount;
        for (ItemStack player_item : player.getInventory().getContents()) {
            if (player_item != null) {
                if (player_item.getType() == material) {
                    int take_next = Math.min(to_take, player_item.getAmount());
                    remove(player, player_item, take_next);
                    to_take -= take_next;
                    if (to_take <= 0) { //Reached amount. Can stop!
                        break;
                    }
                }
            }
        }
    }


    private static void remove(Player p, ItemStack toR, int amount) {
        ItemStack i = toR.clone();
        i.setAmount(amount);
        p.getInventory().removeItem(i);
    }

    /**
     * Check if a player has required item (for quest or shops)
     *
     * @param player    to check
     * @param itemStack to check
     * @param needed    how many items do they need
     * @return true if player has the item
     */
    public static boolean hasItem(Player player, ItemStack itemStack, int needed) {
        if (needed == 0) return true;
        int amount = 0;
        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem != null) {
                if (RunicItemsAPI.isRunicItemSimilar(itemStack, inventoryItem)) {
                    amount += inventoryItem.getAmount();
                    if (amount >= needed) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns a skinned head
     *
     * @param value from minecraft-heads.com
     * @return head item stack for use in menus
     */
    public static ItemStack getHead(String value) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        UUID hashAsId = new UUID(value.hashCode(), value.hashCode());
        return Bukkit.getUnsafe().modifyItemStack(skull,
                "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + value + "\"}]}}}"
        );
    }
}
