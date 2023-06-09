package com.runicrealms.runicitems.weaponskin;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.DonorRank;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record WeaponSkin(
        String id,
        String name,
        Material material,
        int damage,
        CharacterClass classType,
        @Nullable String achievementID,
        @Nullable List<DonorRank> rank,
        String permission
) {

    public boolean hasAchievementID() {
        return this.achievementID != null;
    }

    public boolean hasRank() {
        return this.rank != null;
    }

    public boolean hasPermission() {
        return this.permission != null;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof WeaponSkin && ((WeaponSkin) object).id.equalsIgnoreCase(this.id);
    }

    public void apply(ItemStack itemStack) {
        if (!(itemStack.getItemMeta() instanceof Damageable meta))
            throw new IllegalArgumentException("Cannot apply weapon skin to non-damageable item!");
        meta.setDamage(damage());
        itemStack.setItemMeta((ItemMeta) meta);
        NBTItem nbtItem = new NBTItem(itemStack, true);
        nbtItem.setString("weapon-skin", id());
    }

}
