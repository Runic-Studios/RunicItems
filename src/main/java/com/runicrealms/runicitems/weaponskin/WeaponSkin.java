package com.runicrealms.runicitems.weaponskin;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public record WeaponSkin(
        String id,
        String name,
        Material material,
        int damage,
        @Nullable String achievementID,
        @Nullable String rank,
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
    }

}
