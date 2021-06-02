package com.runicrealms.runicitems.item.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class DisplayableItem {

    private final String displayName;
    private final Material material;
    private final short damage;

    public DisplayableItem(String displayName, Material material, short damage) {
        this.displayName = ChatColor.translateAlternateColorCodes('&', displayName);
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

    public ItemStack generateItem(int count) {
        ItemStack item = new ItemStack(this.material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        }
        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(this.damage);
        }
        meta.setDisplayName(this.displayName);
        item.setItemMeta(meta);
        item.setAmount(count);
        return item;
    }

}
