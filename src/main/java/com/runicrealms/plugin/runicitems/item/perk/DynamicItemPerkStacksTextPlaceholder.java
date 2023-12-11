package com.runicrealms.plugin.runicitems.item.perk;

import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.dynamic.DynamicItemTextPlaceholder;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Used to replace the ? in [?/4] +X item perks
 */
public class DynamicItemPerkStacksTextPlaceholder extends DynamicItemTextPlaceholder {

    private final ItemPerkHandler handler;

    public DynamicItemPerkStacksTextPlaceholder(ItemPerkHandler handler) {
        super(handler.getType().getIdentifier() + "-stacks");
        this.handler = handler;
    }

    public void register() {
        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(this);
    }

    @Nullable
    @Override
    public String generateReplacement(Player viewer, ItemStack item, NBTItem itemNBT, RunicItemTemplate template) {
        int stacks = handler.getCurrentUncappedStacks(viewer);
        if (stacks > handler.getType().getMaxStacks()) {
            return ChatColor.RED.toString() + stacks;
        } else {
            return ChatColor.WHITE.toString() + stacks;
        }
    }

}
