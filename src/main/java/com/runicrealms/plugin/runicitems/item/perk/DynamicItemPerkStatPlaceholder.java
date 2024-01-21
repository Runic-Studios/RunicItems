package com.runicrealms.plugin.runicitems.item.perk;

import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntSupplier;

/**
 * A class that handles the replacement of a stat that stacks
 */
public class DynamicItemPerkStatPlaceholder extends DynamicItemPerkTextPlaceholder {
    private final ItemPerkHandler handler;
    private final IntSupplier supplier;

    public DynamicItemPerkStatPlaceholder(@NotNull String placeholder, @NotNull ItemPerkHandler handler, @NotNull IntSupplier supplier) {
        super(placeholder);
        this.handler = handler;
        this.supplier = supplier;
    }

    @Nullable
    @Override
    public String generateReplacement(Player viewer, ItemStack item, NBTItem itemNBT, RunicItemTemplate template) {
        int base = this.supplier.getAsInt();

        int value;
        if (this.getEquippedSlot(viewer, item, template) != null) { // Item is equipped
            value = this.handler.getCurrentStacks(viewer) * base;
        } else {
            value = itemNBT.getInteger("perks-" + this.handler.getType().getIdentifier()) * base;
        }

        if (value != base) {
            return ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + base + ChatColor.YELLOW + " " + value;
        } else {
            return ChatColor.YELLOW.toString() + base;
        }
    }
}
