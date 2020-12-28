package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.item.inventory.RunicItemOwner;
import com.runicrealms.runicitems.item.stats.RunicArtifactAbility;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemArtifactTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.RunicItemClass;
import com.runicrealms.runicitems.util.ItemIcons;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunicItemArtifact extends RunicItem {

    private final RunicArtifactAbility ability;
    private final RunicItemStatRange damageRange;
    private final LinkedHashMap<RunicItemStatType, RunicItemStat> stats;
    private final int level;
    private final RunicItemRarity rarity;
    private final RunicItemClass runicClass;

    public RunicItemArtifact(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data, int count, long id, RunicItemOwner itemOwner,
                             RunicArtifactAbility ability, RunicItemStatRange damageRange, LinkedHashMap<RunicItemStatType, RunicItemStat> stats,
                             int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(templateId, displayableItem, tags, data, count, id, itemOwner, () -> {
            ItemLoreSection[] sections = new ItemLoreSection[3 + (stats.size() > 0 ? 1 : 0)];
            sections[0] = new ItemLoreSection(new String[] {
                    ChatColor.GRAY + "Required Class: " + ChatColor.WHITE + runicClass.getDisplay(),
                    ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level,
                    rarity.getDisplay()
            });
            sections[1] = new ItemLoreSection(new String[] {
                    ChatColor.RED + "+ " + damageRange.getMin() + "-" + damageRange.getMax() + ItemIcons.SWORD_ICON
            });
            sections[2] = new ItemLoreSection(new String[] {
                    ability.getTrigger().getDisplay() + " " + ChatColor.RESET + "" + ChatColor.GREEN + ability.getAbilityName(),
                    ChatColor.translateAlternateColorCodes('&', ability.getDescription())
            });
            List<String> lore = new ArrayList<String>();
            for (Map.Entry<RunicItemStatType, RunicItemStat> entry : stats.entrySet()) {
                lore.add(
                        entry.getKey().getColor()
                                + (entry.getValue().getRoll() < 0 ? "-" : "+")
                                + entry.getValue().getRoll()
                                + entry.getKey().getSuffix()
                );
            }
            if (stats.size() > 0) {
                sections[3] = new ItemLoreSection(lore);
            }
            return sections;
        });
        this.ability = ability;
        this.damageRange = damageRange;
        this.stats = stats;
        this.level = level;
        this.rarity = rarity;
        this.runicClass = runicClass;
    }

    public RunicItemArtifact(RunicItemArtifactTemplate template, int count, long id, RunicItemOwner itemOwner, LinkedHashMap<RunicItemStatType, RunicItemStat> stats) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id, itemOwner,
                template.getAbility(), template.getDamageRange(), stats,
                template.getLevel(), template.getRarity(), template.getRunicClass()
        );
    }

    public RunicArtifactAbility getAbility() {
        return this.ability;
    }

    public RunicItemStatRange getDamageRange() {
        return this.damageRange;
    }

    public LinkedHashMap<RunicItemStatType, RunicItemStat> getStats() {
        return stats;
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

    @Override
    public void addSpecificItemToData(Data section) {
        for (RunicItemStatType statType : this.stats.keySet()) {
            section.set("stats." + statType.getIdentifier(), this.stats.get(statType).getRollPercentage());
        }
        section.set("damage.min", damageRange.getMin());
        section.set("damage.max", damageRange.getMax());
    }

}
