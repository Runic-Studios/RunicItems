package com.runicrealms.plugin.runicitems.item.perk;

import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Used to replace the ? in [?/4] +X item perks
 */
public class DynamicItemPerkStacksTextPlaceholder extends DynamicItemPerkTextPlaceholder {

    private final ItemPerkHandler handler;

    public DynamicItemPerkStacksTextPlaceholder(ItemPerkHandler handler) {
        super(handler.getType().getIdentifier() + "-equipped");
        this.handler = handler;
    }

    @Nullable
    @Override
    public String generateReplacement(Player viewer, ItemStack item, NBTItem itemNBT, RunicItemTemplate template) {
        int stacks = handler.getCurrentUncappedStacks(viewer);
        if (getEquippedSlot(viewer, item, template) != null) {
            ChatColor stacksColor = stacks > handler.getType().getMaxStacks() ? ChatColor.RED : ChatColor.WHITE;
            return ChatColor.GRAY + "[" + stacksColor + stacks + ChatColor.GRAY + "/" + handler.getType().getMaxStacks() + "] ";
        } else {
            return ChatColor.GRAY + "[" + handler.getType().getMaxStacks() + "] ";
        }
    }

}
