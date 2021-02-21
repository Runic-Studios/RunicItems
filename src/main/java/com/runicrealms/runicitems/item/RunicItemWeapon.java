package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.item.inventory.RunicItemOwner;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.RunicItemClass;
import com.runicrealms.runicitems.util.ItemIcons;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunicItemWeapon extends RunicItem {

    private final RunicItemStatRange damageRange;
    private final LinkedHashMap<RunicItemStatType, RunicItemStat> stats;
    private final int level;
    private final RunicItemRarity rarity;
    private final RunicItemClass runicClass;

    public RunicItemWeapon(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data, int count,
                           RunicItemStatRange damageRange, LinkedHashMap<RunicItemStatType, RunicItemStat> stats,
                           int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(templateId, displayableItem, tags, data, count, () -> {
            ItemLoreSection[] sections = new ItemLoreSection[2 + (stats.size() > 0 ? 1 : 0)];
            sections[0] = new ItemLoreSection(new String[]{
                    ChatColor.GRAY + "Required Class: " + ChatColor.WHITE + runicClass.getDisplay(),
                    ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level,
                    rarity.getDisplay()
            });
            sections[1] = new ItemLoreSection(new String[]{
                    ChatColor.RED + "+ " + damageRange.getMin() + "-" + damageRange.getMax() + ItemIcons.SWORD_ICON
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
                sections[2] = new ItemLoreSection(lore);
            }
            return sections;
        });
        this.damageRange = damageRange;
        this.stats = stats;
        this.level = level;
        this.rarity = rarity;
        this.runicClass = runicClass;
    }

    public RunicItemWeapon(RunicItemWeaponTemplate template, int count, LinkedHashMap<RunicItemStatType, RunicItemStat> stats) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count,
                template.getDamageRange(), stats,
                template.getLevel(), template.getRarity(), template.getRunicClass()
        );
    }

    public RunicItemStatRange getWeaponDamage() {
        return this.damageRange;
    }

    public LinkedHashMap<RunicItemStatType, RunicItemStat> getStats() {
        return stats;
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
    }

}
