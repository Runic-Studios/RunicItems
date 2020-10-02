package com.runicrealms.runicitems.item.template;

import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.Material;

import java.util.List;

public abstract class RunicItemTemplate {

    protected String id;
    protected String itemName;
    protected Material material;
    protected List<RunicItemTag> tags;

    public RunicItemTemplate(String id, String itemName, Material material, List<RunicItemTag> tags) {
        this.id = id;
        this.itemName = itemName;
        this.material = material;
        this.tags = tags;
    }

    public abstract void generateItem();

}
