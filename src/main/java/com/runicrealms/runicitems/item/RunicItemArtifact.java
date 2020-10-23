package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicSpell;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.RunicItemClass;
import com.runicrealms.runicitems.item.util.SpellClickTrigger;
import com.runicrealms.runicitems.util.ItemIcons;
import org.bukkit.ChatColor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunicItemArtifact extends RunicItem {

    private LinkedHashMap<SpellClickTrigger, RunicSpell> spells;
    private RunicItemStatRange damageRange;
    private int level;
    private RunicItemRarity rarity;
    private RunicItemClass runicClass;

    public RunicItemArtifact(String id, DisplayableItem displayableItem, List<RunicItemTag> tags,
                             LinkedHashMap<SpellClickTrigger, RunicSpell> spells, RunicItemStatRange damageRange,
                             int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(id, displayableItem, tags, () -> {
            ItemLoreSection[] sections = new ItemLoreSection[2 + spells.size()];
            sections[0] = new ItemLoreSection(new String[] {
                    rarity.getDisplay(),
                    ChatColor.GRAY + "Required Class: " + ChatColor.WHITE + runicClass.getDisplay(),
                    ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level
            });
            sections[1] = new ItemLoreSection(new String[] {
                    ChatColor.RED + "+ " + damageRange.getMin() + "-" + damageRange.getMax() + ItemIcons.ATTACK_ICON
            });
            int counter = 2;
            for (Map.Entry<SpellClickTrigger, RunicSpell> entry : spells.entrySet()) {
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
        this.runicClass = runicClass;
    }

    public LinkedHashMap<SpellClickTrigger, RunicSpell> getSpells() {
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

    public RunicItemClass getRunicClass() {
        return this.runicClass;
    }

}
