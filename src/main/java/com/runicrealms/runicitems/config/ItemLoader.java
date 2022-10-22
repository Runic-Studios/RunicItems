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

import java.util.*;
import java.util.logging.Level;

public class ItemLoader {

    /**
     * Loads an item from a MongoDataSection
     *
     * @param section
     * @param id
     * @return
     */
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
                            if (!statKey.equalsIgnoreCase("health")
                                    && !statKey.equalsIgnoreCase("main")
                                    && !statKey.equalsIgnoreCase("tier")) {
                                gemStats.put(Stat.getFromIdentifier(statKey), section.get("gems." + gemKey + "." + statKey, Integer.class));
                            }
                        }
                        gemBonuses.add(new GemBonus(
                                gemStats,
                                section.has("gems." + gemKey + ".health") ? section.get("gems." + gemKey + ".health", Integer.class) : 0,
                                Stat.getFromIdentifier(section.get("gems." + gemKey + ".main", String.class)),
                                section.get("gems." + gemKey + ".tier", Integer.class)));
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
                return new RunicItemGem(gemTemplate, count, id, new GemBonus(
                        loadGemStats(section),
                        section.has("health") ? section.get("health", Integer.class) : 0,
                        gemTemplate.getMainStat(),
                        gemTemplate.getTier()));
            }
        } catch (Exception exception) {
            Bukkit.getLogger().log(Level.INFO, "[RunicItems] Error loading item from mongo!");
            exception.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * Loads an item from redis
     *
     * @param itemDataMap
     * @param id
     * @return
     */
    public static RunicItem loadItem(Map<String, String> itemDataMap, long id) {
        try {
            String templateId = itemDataMap.get("template-id");
            int count = Integer.parseInt(itemDataMap.get("count"));
            RunicItemTemplate template = TemplateManager.getTemplateFromId(templateId);
            if (template instanceof RunicItemArmorTemplate) {
                List<GemBonus> gemBonuses = new ArrayList<>();
//                if (section.has("gems")) { todo: gems
//                    for (String gemKey : section.getSection("gems").getKeys()) {
//                        LinkedHashMap<Stat, Integer> gemStats = new LinkedHashMap<>();
//                        for (String statKey : section.getSection("gems." + gemKey).getKeys()) {
//                            if (!statKey.equalsIgnoreCase("health")
//                                    && !statKey.equalsIgnoreCase("main")
//                                    && !statKey.equalsIgnoreCase("tier")) {
//                                gemStats.put(Stat.getFromIdentifier(statKey), section.get("gems." + gemKey + "." + statKey, Integer.class));
//                            }
//                        }
//                        gemBonuses.add(new GemBonus(
//                                gemStats,
//                                section.has("gems." + gemKey + ".health") ? section.get("gems." + gemKey + ".health", Integer.class) : 0,
//                                Stat.getFromIdentifier(section.get("gems." + gemKey + ".main", String.class)),
//                                section.get("gems." + gemKey + ".tier", Integer.class)));
//                    }
//                }
                RunicItemArmorTemplate armorTemplate = (RunicItemArmorTemplate) template;
                return new RunicItemArmor(armorTemplate, count, id, loadStats(itemDataMap, armorTemplate.getStats()), gemBonuses);
            } else if (template instanceof RunicItemArtifactTemplate) {
                RunicItemArtifactTemplate artifactTemplate = (RunicItemArtifactTemplate) template;
                return new RunicItemArtifact(artifactTemplate, count, id, loadStats(itemDataMap, artifactTemplate.getStats()));
            } else if (template instanceof RunicItemBookTemplate) {
                RunicItemBookTemplate bookTemplate = (RunicItemBookTemplate) template;
                return new RunicItemBook(bookTemplate, count, id);
            } else if (template instanceof RunicItemDynamicTemplate) {
                int dynamicField = Integer.parseInt(itemDataMap.get(RunicItemDynamic.getDynamicFieldString()));
                return new RunicItemDynamic((RunicItemDynamicTemplate) template, count, id, dynamicField);
            } else if (template instanceof RunicItemGenericTemplate) {
                return new RunicItemGeneric((RunicItemGenericTemplate) template, count, id);
            } else if (template instanceof RunicItemOffhandTemplate) {
                RunicItemOffhandTemplate offhandTemplate = (RunicItemOffhandTemplate) template;
                return new RunicItemOffhand(offhandTemplate, count, id, loadStats(itemDataMap, offhandTemplate.getStats()));
            } else if (template instanceof RunicItemWeaponTemplate) {
                RunicItemWeaponTemplate weaponTemplate = (RunicItemWeaponTemplate) template;
                return new RunicItemWeapon(weaponTemplate, count, id, loadStats(itemDataMap, weaponTemplate.getStats()));
            } else if (template instanceof RunicItemGemTemplate) {
                RunicItemGemTemplate gemTemplate = (RunicItemGemTemplate) template;
                return new RunicItemGem(gemTemplate, count, id, new GemBonus(
                        loadGemStats(itemDataMap),
                        itemDataMap.get("health") != null ? Integer.parseInt(itemDataMap.get("health")) : 0,
                        gemTemplate.getMainStat(),
                        gemTemplate.getTier()));
            }
        } catch (Exception exception) {
            Bukkit.getLogger().log(Level.INFO, "[RunicItems] Error loading item from redis!");
            exception.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * @param section
     * @param templateStats
     * @return
     */
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

    /**
     * @param itemDataMap
     * @param templateStats
     * @return
     */
    private static LinkedHashMap<Stat, RunicItemStat> loadStats(Map<String, String> itemDataMap, Map<Stat, RunicItemStatRange> templateStats) {
        Map<Stat, RunicItemStat> stats = new HashMap<>();
        for (Stat templateStatType : templateStats.keySet()) {
            if (itemDataMap.containsKey("stats:" + templateStatType.getIdentifier())) { // Item has stat and has already been rolled (stored in database)
                double stat = Double.parseDouble(itemDataMap.get("stats:" + templateStatType.getIdentifier()));
                stats.put(templateStatType, new RunicItemStat(templateStats.get(templateStatType), stat));
            } else { // Item has recently been added this stat and the roll does not exist in database, so make a new roll
                stats.put(templateStatType, new RunicItemStat(templateStats.get(templateStatType)));
            }
        }
        return StatUtil.sortStatMap(stats);
    }

    /**
     * @param itemDataMap
     * @return
     */
    private static LinkedHashMap<Stat, Integer> loadGemStats(Map<String, String> itemDataMap) { // , Map<Stat, RunicItemStatRange> templateStats
        Map<Stat, Integer> stats = new HashMap<>();

        for (Stat stat : Stat.values()) { // for all POSSIBLE values of stat
            if (itemDataMap.containsKey("gem-stats:" + stat.getIdentifier())) { // Item has stat and has already been rolled (stored in database)
                int statValue = Integer.parseInt(itemDataMap.get("gem-stats:" + stat.getIdentifier()));
                stats.put(stat, statValue);
            }
        }


//        if (section.has("gem-stats")) {
//            Data gemStatsSection = section.getSection("gem-stats");
//            for (String key : gemStatsSection.getKeys()) {
//                Stat stat = Stat.getFromIdentifier(key);
//                if (stat == null) continue;
//                stats.put(stat, gemStatsSection.get(key, Integer.class));
//            }
//        }
        return StatUtil.sortStatMap(stats);
    }

    /**
     * @param section
     * @return
     */
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
