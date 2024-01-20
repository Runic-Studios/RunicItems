package com.runicrealms.plugin.runicitems.item.perk;

import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleSupplier;

/**
 * A class that handles the replacement of a stat that uses percentages
 */
public class DynamicItemPerkPercentStatPlaceholder extends DynamicItemPerkTextPlaceholder {
    private final ItemPerkHandler handler;
    private final DoubleSupplier supplier;

    public DynamicItemPerkPercentStatPlaceholder(@NotNull String placeholder, @NotNull ItemPerkHandler handler, @NotNull DoubleSupplier supplier) {
        super(placeholder);
        this.handler = handler;
        this.supplier = supplier;
    }

    @Nullable
    @Override
    public String generateReplacement(Player viewer, ItemStack item, NBTItem itemNBT, RunicItemTemplate template) {
        double basePercentage = roundToNearestHundredth(this.supplier.getAsDouble() * 100);

        double percentage;
        if (this.getEquippedSlot(viewer, item, template) != null) { // Item is equipped
            percentage = this.handler.getCurrentStacks(viewer) * basePercentage;
        } else {
            percentage = itemNBT.getInteger("perks-" + this.handler.getType().getIdentifier()) * basePercentage;
        }

        if (percentage != basePercentage) {
            return ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + basePercentage + "%" + ChatColor.YELLOW + " " + percentage + "%";
        } else {
            return ChatColor.YELLOW.toString() + basePercentage + "%";
        }
    }

    private static double roundToNearestHundredth(double number) {
        return Math.round(number * 100.0) / 100.0;
    }
}
