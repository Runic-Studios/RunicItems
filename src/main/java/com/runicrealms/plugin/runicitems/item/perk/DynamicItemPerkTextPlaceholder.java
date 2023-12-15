package com.runicrealms.plugin.runicitems.item.perk;

import com.runicrealms.plugin.common.util.ArmorType;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.dynamic.DynamicItemTextPlaceholder;
import com.runicrealms.plugin.runicitems.item.RunicItemArmor;
import com.runicrealms.plugin.runicitems.item.RunicItemOffhand;
import com.runicrealms.plugin.runicitems.item.RunicItemWeapon;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.plugin.runicitems.player.PlayerEquipmentCache;
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
        PlayerEquipmentCache cache = RunicItemsAPI.getCachedPlayerItems(player.getUniqueId());
        if (cache == null) return null; // We haven't loaded yet

        if (ArmorType.matchType(item) != null) {
            RunicItemArmor helmet = cache.getHelmet();
            ItemStack itemHelmet = player.getInventory().getHelmet();
            if (helmet != null
                    && itemHelmet != null
                    && helmet.getTemplateId().equals(template.getId())
                    && itemHelmet.equals(item)) return EquippedSlot.HELMET;

            RunicItemArmor chestplate = cache.getChestplate();
            ItemStack itemChestplate = player.getInventory().getChestplate();
            if (chestplate != null
                    && itemChestplate != null
                    && chestplate.getTemplateId().equals(template.getId())
                    && itemChestplate.equals(item)) return EquippedSlot.CHESTPLATE;

            RunicItemArmor leggings = cache.getLeggings();
            ItemStack itemLeggings = player.getInventory().getLeggings();
            if (leggings != null
                    && itemLeggings != null
                    && leggings.getTemplateId().equals(template.getId())
                    && itemLeggings.equals(item)) return EquippedSlot.LEGGINGS;

            RunicItemArmor boots = cache.getBoots();
            ItemStack itemBoots = player.getInventory().getBoots();
            if (boots != null
                    && itemBoots != null
                    && boots.getTemplateId().equals(template.getId())
                    && itemBoots.equals(item)) return EquippedSlot.BOOTS;
        }

        RunicItemOffhand offhand = cache.getOffhand();
        ItemStack itemOffhand = player.getInventory().getItemInOffHand();
        if (offhand != null
                && offhand.getTemplateId().equals(template.getId())
                && itemOffhand.equals(item)) return EquippedSlot.OFFHAND;

        RunicItemWeapon weapon = cache.getWeapon();
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
