package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RunicItem extends RunicNbtItem {

    private ItemStack currentItem; // ItemStack that we are currently displaying to the player

    private RunicDisplayableItem displayableItem; // Base ItemStack information that we get from the template
    private String id; // Template ID
    private List<RunicItemTag> tags; // List of tags (soulbound, untradeable, etc)

    public RunicItem(String id, String displayName, Material material, short damage, List<RunicItemTag> tags) {
        this.id = id;
        this.displayableItem = new RunicDisplayableItem(displayName, material, damage);
        this.tags = tags;
        // TODO - generate currentItem
    }

    @Override
    public ItemStack getCurrentItem() {
        return this.currentItem;
    }

    public RunicDisplayableItem getDisplayableItem() {
        return this.displayableItem;
    }

    public String getId() {
        return this.id;
    }

    public List<RunicItemTag> getTags() {
        return this.tags;
    }

}
