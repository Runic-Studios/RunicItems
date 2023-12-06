package com.runicrealms.plugin.runicitems.dynamic;

import com.runicrealms.plugin.runicitems.item.RunicItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class DynamicItemTextPlaceholder {

    protected final String placeholder; // Placeholder without the <>

    private final String toReplace;

    protected DynamicItemTextPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        this.toReplace = "<" + this.placeholder + ">";
    }

    /**
     * Determines if a given item stack (with a viewing player) is a target for this placeholder.
     * For example, a level requirement placeholder should only target those with NBT matching runic item weapons
     * WARNING: This method WILL BE run HUNDREDS of times per TICK, DO NOT attempt to process the ItemStack into a RunicItem.
     * If you wish to target all items, you can always return true for this.
     * The idea behind this check is to prevent the generation of replacements for items that do not necessarily need them.
     */
    protected abstract boolean isTarget(Player viewer, ItemStack item);

    /**
     * Generates the text replacement for this placeholder for a given player
     * WARNING: be careful if you plan to turn this ItemStack into a RunicItem!
     * Any complex operations performed in this function will be repeated potentially hundreds of time per second or even per tick.
     * Be as efficient with replacements as you can, caching values if needed.
     */
    protected abstract String createReplacement(Player viewer, ItemStack item);

    public void processItem(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.getDisplayName().con)
            String newName = meta.getDisplayName().replace(toReplace, createReplacement())
        }
    }

}
