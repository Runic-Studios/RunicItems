package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicSpellType;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.SpellClickTrigger;
import com.runicrealms.runicitems.util.ItemIcons;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunicItemArtifact extends RunicItem {

    private LinkedHashMap<SpellClickTrigger, RunicSpellType> spells;
    private RunicItemStatRange damageRange;
    private int level;
    private RunicItemRarity rarity;

    public RunicItemArtifact(String id, String itemName, Material material, short damage, List<RunicItemTag> tags,
                             LinkedHashMap<SpellClickTrigger, RunicSpellType> spells, RunicItemStatRange damageRange,
                             int level, RunicItemRarity rarity) {
        super(id, itemName, material, damage, tags, () -> {
            ItemLoreSection[] sections = new ItemLoreSection[2 + spells.size()];
            sections[0] = new ItemLoreSection(new String[] {
                    rarity.getDisplay(),
                    ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level
            });
            sections[1] = new ItemLoreSection(new String[] {
                    ChatColor.RED + "+ " + damageRange.getMin() + "-" + damageRange.getMax() + ItemIcons.ATTACK_ICON
            });
            int counter = 2;
            for (Map.Entry<SpellClickTrigger, RunicSpellType> entry : spells.entrySet()) {
                sections[counter] = new ItemLoreSection(new String[] {
                        entry.getKey().getDisplay() + " " + ChatColor.RESET + "" + ChatColor.GREEN + entry.getValue().getSpellName(),
                        ChatColor.translateAlternateColorCodes('&', entry.getValue().getDescription())
                });
                counter++;
            }
            return sections;
        });
        this.spells = spells;
        this.damageRange = damageRange;
        this.level = level;
        this.rarity = rarity;
    }

    public LinkedHashMap<SpellClickTrigger, RunicSpellType> getSpells() {
        return this.spells;
    }

    public RunicItemStatRange getDamageRange() {
        return this.damageRange;
    }

    public int getRandomDamage() {
        return this.damageRange.getRandomValue();
    }

    public int getLevel() {
        return this.level;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

}
