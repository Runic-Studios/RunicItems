package com.runicrealms.plugin.runicitems.dynamic;

import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class DynamicItemTextPlaceholder {

    protected final String placeholder; // Placeholder without the <>

    protected DynamicItemTextPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getIdentifier() {
        return this.placeholder;
    }

    /**
     * Generates the text replacement for this placeholder for a given player
     * WARNING: you should not be converting this item stack into a RunicItem!
     * Any complex operations performed in this function will be repeated potentially hundreds of time per second or even per tick.
     * Be as efficient with replacements as you can, caching values if needed.
     * <p>
     * Null indicates no replacement
     */
    public abstract @Nullable String generateReplacement(Player viewer, ItemStack item, NBTItem itemNBT, RunicItemTemplate template);

}
