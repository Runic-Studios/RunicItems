package com.runicrealms.plugin.runicitems.item.perk;

import com.runicrealms.plugin.common.util.ArmorType;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.dynamic.DynamicItemTextPlaceholder;
import com.runicrealms.plugin.runicitems.item.RunicItemArmor;
import com.runicrealms.plugin.runicitems.item.RunicItemOffhand;
import com.runicrealms.plugin.runicitems.item.RunicItemWeapon;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public abstract class DynamicItemPerkTextPlaceholder extends DynamicItemTextPlaceholder {

    protected DynamicItemPerkTextPlaceholder(String placeholder) {
        super(placeholder);
    }

    /**
     * Gets the slot in which an item is currently equipped
     * Returns null if not equipped
     */
    protected @Nullable EquippedSlot getEquippedSlot(Player player, ItemStack item, RunicItemTemplate template) {
        if (ArmorType.matchType(item) != null) {
            RunicItemArmor helmet = RunicItemsAPI.getCachedPlayerItems(player.getUniqueId()).getHelmet();
            ItemStack itemHelmet = player.getInventory().getHelmet();
            if (helmet != null
                    && itemHelmet != null
                    && helmet.getTemplateId().equals(template.getId())
                    && itemHelmet.equals(item)) return EquippedSlot.HELMET;

            RunicItemArmor chestplate = RunicItemsAPI.getCachedPlayerItems(player.getUniqueId()).getChestplate();
            ItemStack itemChestplate = player.getInventory().getChestplate();
            if (chestplate != null
                    && itemChestplate != null
                    && chestplate.getTemplateId().equals(template.getId())
                    && itemChestplate.equals(item)) return EquippedSlot.CHESTPLATE;

            RunicItemArmor leggings = RunicItemsAPI.getCachedPlayerItems(player.getUniqueId()).getLeggings();
            ItemStack itemLeggings = player.getInventory().getLeggings();
            if (leggings != null
                    && itemLeggings != null
                    && leggings.getTemplateId().equals(template.getId())
                    && itemLeggings.equals(item)) return EquippedSlot.LEGGINGS;

            RunicItemArmor boots = RunicItemsAPI.getCachedPlayerItems(player.getUniqueId()).getBoots();
            ItemStack itemBoots = player.getInventory().getBoots();
            if (boots != null
                    && itemBoots != null
                    && boots.getTemplateId().equals(template.getId())
                    && itemBoots.equals(item)) return EquippedSlot.BOOTS;
        }

        RunicItemOffhand offhand = RunicItemsAPI.getCachedPlayerItems(player.getUniqueId()).getOffhand();
        ItemStack itemOffhand = player.getInventory().getItemInOffHand();
        if (offhand != null
                && offhand.getTemplateId().equals(template.getId())
                && itemOffhand.equals(item)) return EquippedSlot.OFFHAND;

        RunicItemWeapon weapon = RunicItemsAPI.getCachedPlayerItems(player.getUniqueId()).getWeapon();
        ItemStack itemWeapon = player.getInventory().getItemInMainHand();
        if (weapon != null
                && weapon.getTemplateId().equals(template.getId())
                && itemWeapon.equals(item)) return EquippedSlot.WEAPON;

        return null;
    }

    protected enum EquippedSlot {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS,
        OFFHAND,
        WEAPON;
    }

}
