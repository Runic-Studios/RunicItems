package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RunicItem extends RunicNbtItem {

    private ItemStack itemStack;

    private String id;
    private String itemName;
    private Material material;
    private short damage;
    private List<RunicItemTag> tags;

    public RunicItem(String id, String itemName, Material material, short damage, List<RunicItemTag> tags) {
        this.id = id;
        this.itemName = itemName;
        this.material = material;
        this.damage = damage;
        this.tags = tags;
    }

    @Override
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public String getId() {
        return this.id;
    }

    public String getItemName() {
        return this.itemName;
    }

    public Material getMaterial() {
        return this.material;
    }

    public short getDamage() {
        return this.damage;
    }

    public List<RunicItemTag> getTags() {
        return this.tags;
    }

}
