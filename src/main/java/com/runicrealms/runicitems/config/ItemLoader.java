package com.runicrealms.runicitems.config;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.Stat;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.*;
import com.runicrealms.runicitems.item.stats.GemBonus;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.template.*;
import com.runicrealms.runicitems.util.StatUtil;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class ItemLoader {

    public static RunicItem loadItem(Data section, long id) {
        try {
            String templateId = section.get("template-id", String.class);
            int count = section.get("count", Integer.class);
            RunicItemTemplate template = TemplateManager.getTemplateFromId(templateId);
            if (template instanceof RunicItemArmorTemplate) {
                List<GemBonus> gemBonuses = new ArrayList<>();
                if (section.has("gems")) {
                    for (String gemKey : section.getSection("gems").getKeys()) {
                        LinkedHashMap<Stat, Integer> gemStats = new LinkedHashMap<>();
                        for (String statKey : section.getSection("gems." + gemKey).getKeys()) {
                            if (!statKey.equalsIgnoreCase("health")) {
                                gemStats.put(Stat.getFromIdentifier(statKey), section.get("gems." + gemKey + "." + statKey, Integer.class));
                            }
                        }
                        gemBonuses.add(new GemBonus(gemStats, section.get("gems." + gemKey + ".health", Integer.class)));
                    }
                }
                RunicItemArmorTemplate armorTemplate = (RunicItemArmorTemplate) template;
                return new RunicItemArmor(armorTemplate, count, id, loadStats(section, armorTemplate.getStats()), gemBonuses);
            } else if (template instanceof RunicItemArtifactTemplate) {
                RunicItemArtifactTemplate artifactTemplate = (RunicItemArtifactTemplate) template;
                return new RunicItemArtifact(artifactTemplate, count, id, loadStats(section, artifactTemplate.getStats()));
            } else if (template instanceof RunicItemBookTemplate) {
                RunicItemBookTemplate bookTemplate = (RunicItemBookTemplate) template;
                return new RunicItemBook(bookTemplate, count, id);
            } else if (template instanceof RunicItemDynamicTemplate) {
                int dynamicField = section.get(RunicItemDynamic.getDynamicFieldString(), Integer.class);
                return new RunicItemDynamic((RunicItemDynamicTemplate) template, count, id, dynamicField);
            } else if (template instanceof RunicItemGenericTemplate) {
                return new RunicItemGeneric((RunicItemGenericTemplate) template, count, id);
            } else if (template instanceof RunicItemOffhandTemplate) {
                RunicItemOffhandTemplate offhandTemplate = (RunicItemOffhandTemplate) template;
                return new RunicItemOffhand(offhandTemplate, count, id, loadStats(section, offhandTemplate.getStats()));
            } else if (template instanceof RunicItemWeaponTemplate) {
                RunicItemWeaponTemplate weaponTemplate = (RunicItemWeaponTemplate) template;
                return new RunicItemWeapon(weaponTemplate, count, id, loadStats(section, weaponTemplate.getStats()));
            } else if (template instanceof RunicItemGemTemplate) {
                RunicItemGemTemplate gemTemplate = (RunicItemGemTemplate) template;
                return new RunicItemGem(gemTemplate, count, id, loadGemStats(section), section.has("health") ? section.get("health", Integer.class) : 0);
            }
        } catch (Exception exception) {
            Bukkit.getLogger().log(Level.INFO, "[RunicItems] Error loading item!");
            exception.printStackTrace();
            return null;
        }
        return null;
    }

    private static LinkedHashMap<Stat, RunicItemStat> loadStats(Data section, Map<Stat, RunicItemStatRange> templateStats) {
        Map<Stat, RunicItemStat> stats = new HashMap<>();
        if (section.has("stats")) {
            Set<String> sectionKeys = section.getSection("stats").getKeys();
            for (Stat templateStatType : templateStats.keySet()) {
                if (sectionKeys.contains(templateStatType.getIdentifier())) { // Item has stat and has already been rolled (stored in database)
                    stats.put(templateStatType, new RunicItemStat(templateStats.get(templateStatType), section.get("stats." + templateStatType.getIdentifier(), Double.class)));
                } else { // Item has recently been added this stat and the roll does not exist in database, so make a new roll
                    stats.put(templateStatType, new RunicItemStat(templateStats.get(templateStatType)));
                }
            }
        }
        return StatUtil.sortStatMap(stats);
    }

    private static LinkedHashMap<Stat, Integer> loadGemStats(Data section) {
        Map<Stat, Integer> stats = new HashMap<>();
        if (section.has("gem-stats")) {
            Data gemStatsSection = section.getSection("gem-stats");
            for (String key : gemStatsSection.getKeys()) {
                Stat stat = Stat.getFromIdentifier(key);
                if (stat == null) continue;
                stats.put(stat, gemStatsSection.get(key, Integer.class));
            }
        }
        return StatUtil.sortStatMap(stats);
    }

}
