package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RunicItemOffhand extends RunicItem {

    private Map<RunicItemStatType, RunicItemStat> stats;
    private int level;
    private RunicItemRarity rarity;

    public RunicItemOffhand(String id, String itemName, Material material, short damage, List<RunicItemTag> tags,
                            Map<RunicItemStatType, RunicItemStat> stats,
                            int level, RunicItemRarity rarity) {
        super(id, itemName, material, damage, tags, () -> {
            List<String> lore = new ArrayList<String>();
            for (Map.Entry<RunicItemStatType, RunicItemStat> entry : stats.entrySet()) {
                lore.add(
                        entry.getKey().getColor()
                                + (entry.getValue().getRoll() < 0 ? "-" : "+")
                                + entry.getValue().getRoll()
                                + entry.getKey().getSuffix()
                );
            }
            return new ItemLoreSection[] {
                    new ItemLoreSection(new String[] {
                            rarity.getDisplay(),
                            ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level,
                    }),
                    new ItemLoreSection(lore)
            };
        });
        this.stats = stats;
        this.level = level;
        this.rarity = rarity;
    }

    public Map<RunicItemStatType, RunicItemStat> getStats() {
        return this.stats;
    }

    public int getLevel() {
        return this.level;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

}
