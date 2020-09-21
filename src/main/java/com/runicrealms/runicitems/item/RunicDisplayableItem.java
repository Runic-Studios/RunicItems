package com.runicrealms.runicitems.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class RunicDisplayableItem {

    private String displayName;
    private Material material;
    private short damage;

    public RunicDisplayableItem(String displayName, Material material, short damage) {
        this.displayName = displayName;
        this.material = material;
        this.damage = damage;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Material getMaterial() {
        return this.material;
    }

    public short getDamage() {
        return this.damage;
    }

    public ItemStack generateItem() {
        ItemStack item = new ItemStack(this.material);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(this.damage);
        }
        meta.setDisplayName(this.displayName);
        item.setItemMeta(meta);
        return item;
    }

}
