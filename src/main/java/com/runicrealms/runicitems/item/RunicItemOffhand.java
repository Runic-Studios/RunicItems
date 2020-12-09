package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemOffhandTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunicItemOffhand extends RunicItem {

    private final LinkedHashMap<RunicItemStatType, RunicItemStat> stats;
    private final int level;
    private final RunicItemRarity rarity;

    public RunicItemOffhand(String id, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, Object> data, int count,
                            LinkedHashMap<RunicItemStatType, RunicItemStat> stats,
                            int level, RunicItemRarity rarity) {
        super(id, displayableItem, tags, data, count, () -> {
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

    public RunicItemOffhand(RunicItemOffhandTemplate template, int count, LinkedHashMap<RunicItemStatType, RunicItemStat> stats) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count,
                stats,
                template.getLevel(), template.getRarity()
        );
    }

    public LinkedHashMap<RunicItemStatType, RunicItemStat> getStats() {
        return this.stats;
    }

    public int getLevel() {
        return this.level;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

    @Override
    public void addSpecificItemToData(Data section) {
        for (RunicItemStatType statType : this.stats.keySet()) {
            section.set("stats." + statType.getIdentifier(), this.stats.get(statType).getRollPercentage());
        }
    }

}
