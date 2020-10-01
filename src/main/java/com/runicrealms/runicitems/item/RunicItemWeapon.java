package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.util.ItemIcons;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.List;

public class RunicItemWeapon extends RunicItem {

    private RunicItemStatRange damageRange;
    private int level;
    private RunicItemRarity rarity;

    public RunicItemWeapon(String id, String itemName, Material material, short damage, List<RunicItemTag> tags,
                           RunicItemStatRange damageRange,
                           int level, RunicItemRarity rarity) {
        super(id, itemName, material, damage, tags, () -> new ItemLoreSection[] {
                new ItemLoreSection(new String[] {
                        rarity.getDisplay(),
                        ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level
                }),
                new ItemLoreSection(new String[] {
                        ChatColor.RED + "+ " + damageRange.getMin() + "-" + damageRange.getMax() + ItemIcons.ATTACK_ICON
                })
        });
        this.damageRange = damageRange;
        this.level = level;
        this.rarity = rarity;
    }

    public RunicItemStatRange getWeaponDamage() {
        return this.damageRange;
    }

    public int getLevel() {
        return this.level;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

}
