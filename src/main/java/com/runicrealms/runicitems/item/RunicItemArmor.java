package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.item.inventory.RunicItemOwner;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.RunicItemClass;
import com.runicrealms.runicitems.util.ItemIcons;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunicItemArmor extends RunicItem {

    private final int level;
    private final RunicItemRarity rarity;
    private final int health;
    private final LinkedHashMap<RunicItemStatType, RunicItemStat> stats;
    private final List<LinkedHashMap<RunicItemStatType, Integer>> gems;
    private final int maxGemSlots;
    private final RunicItemClass runicClass;

    public RunicItemArmor(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data, int count,
                          int health, LinkedHashMap<RunicItemStatType, RunicItemStat> stats, List<LinkedHashMap<RunicItemStatType, Integer>> gems, int maxGemSlots,
                          int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(templateId, displayableItem, tags, data, count, () -> {
            List<String> lore = new ArrayList<String>();
            for (Map.Entry<RunicItemStatType, RunicItemStat> entry : stats.entrySet()) {
                int finalValue = entry.getValue().getRoll();
                for (LinkedHashMap<RunicItemStatType, Integer> gem : gems) {
                    if (gem.containsKey(entry.getKey())) {
                        finalValue += gem.get(entry.getKey());
                    }
                }
                lore.add(
                        entry.getKey().getColor()
                                + (finalValue < 0 ? "-" : "+")
                                + (finalValue != entry.getValue().getRoll() ?
                                ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + entry.getValue().getRoll() + ChatColor.RESET + "" + entry.getKey().getColor()
                                : "")
                                + entry.getValue().getRoll()
                                + entry.getKey().getSuffix()
                );
            }
            return new ItemLoreSection[] {
                    new ItemLoreSection(new String[] {
                            ChatColor.GRAY + "Required Class: " + ChatColor.WHITE + runicClass.getDisplay(),
                            ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level,
                            ChatColor.GRAY + "[" + ChatColor.WHITE + gems.size() + ChatColor.GRAY + "/" + ChatColor.WHITE + maxGemSlots + ChatColor.GRAY + "] Gems",
                            rarity.getDisplay()
                    }),
                    new ItemLoreSection(new String[] {
                            ChatColor.RED + "" + health + ItemIcons.HEART_ICON + " Health"
                    }),
                    new ItemLoreSection(lore)
            };
        });
        this.rarity = rarity;
        this.level = level;
        this.health = health;
        this.gems = gems;
        this.stats = stats;
        this.maxGemSlots = maxGemSlots;
        this.runicClass = runicClass;
    }

    public RunicItemArmor(RunicItemArmorTemplate template, int count, LinkedHashMap<RunicItemStatType, RunicItemStat> stats, List<LinkedHashMap<RunicItemStatType, Integer>> gems) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count,
                template.getHealth(), stats, gems, template.getMaxGemSlots(),
                template.getLevel(), template.getRarity(), template.getRunicClass()
        );
    }

    public int getHealth() {
        return this.health;
    }

    public LinkedHashMap<RunicItemStatType, RunicItemStat> getStats() {
        return this.stats;
    }

    public List<LinkedHashMap<RunicItemStatType, Integer>> getGems() {
        return this.gems;
    }

    public int getLevel() {
        return this.level;
    }

    public int getMaxGemSlots() {
        return this.maxGemSlots;
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
        int count = 0;
        for (LinkedHashMap<RunicItemStatType, Integer> gem : this.gems) {
            for (RunicItemStatType statType : gem.keySet()) {
                section.set("gems." + count + "." + statType, gem.get(statType));
            }
            count++;
        }
    }

}
