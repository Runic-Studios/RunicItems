package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.Material;

import java.util.List;

public class RunicItemWeapon extends RunicItem {

    private RunicItemStatRange weaponDamage;
    private int level;

    public RunicItemWeapon(String id, String itemName, Material material, short damage, List<RunicItemTag> tags, RunicItemStatRange weaponDamage, int level) {
        super(id, itemName, material, damage, tags);
        this.weaponDamage = weaponDamage;
        this.level = level;
    }

    public RunicItemStatRange getWeaponDamage() {
        return this.weaponDamage;
    }

    public int getLevel() {
        return this.level;
    }

}
