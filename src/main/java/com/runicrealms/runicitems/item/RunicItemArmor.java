package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunicItemArmor extends RunicItem {

    private int level;
    private RunicItemRarity rarity;
    private LinkedHashMap<RunicItemStatType, RunicItemStat> stats;
    private LinkedHashMap<RunicItemStatType, Integer> gems;
    private int maxGemSlots;

    public RunicItemArmor(String id, String itemName, Material material, short damage, List<RunicItemTag> tags,
                          LinkedHashMap<RunicItemStatType, RunicItemStat> stats, LinkedHashMap<RunicItemStatType, Integer> gems, int maxGemSlots,
                          int level, RunicItemRarity rarity) {
        super(id, itemName, material, damage, tags, () -> {
            List<String> lore = new ArrayList<String>();
            for (Map.Entry<RunicItemStatType, RunicItemStat> entry : stats.entrySet()) {
                int finalValue = gems.containsKey(entry.getKey()) ? gems.get(entry.getKey()) + entry.getValue().getRoll() : entry.getValue().getRoll();
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
                            rarity.getDisplay(),
                            ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level,
                            ChatColor.GRAY + "[" + ChatColor.WHITE + gems.size() + ChatColor.GRAY + "/" + ChatColor.WHITE + maxGemSlots + ChatColor.GRAY + "] Gems"
                    }),
                    new ItemLoreSection(lore)
            };
        });
        this.rarity = rarity;
        this.gems = gems;
        this.level = level;
        this.stats = stats;
        this.maxGemSlots = maxGemSlots;
    }

    public LinkedHashMap<RunicItemStatType, RunicItemStat> getStats() {
        return this.stats;
    }

    public LinkedHashMap<RunicItemStatType, Integer> getGems() {
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

}
