package com.runicrealms.runicitems.config;

import com.runicrealms.runicitems.Stat;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.*;
import com.runicrealms.runicitems.item.stats.GemBonus;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.template.*;
import com.runicrealms.runicitems.util.StatUtil;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ItemLoader {

    private static final int MAX_GEM_SLOTS = 10;

    /**
     * Reads a document from mongo and converts it to the appropriate item type
     *
     * @param document from mongo representing the individual item
     * @param id       for the dupe manager
     * @return some child class of RunicItem
     */
    public static RunicItem loadFromDocument(Document document, long id) {
        try {
            String templateId = document.getString("template-id");
            int count = document.getInteger("count");
            RunicItemTemplate template = TemplateManager.getTemplateFromId(templateId);
            if (template instanceof RunicItemArmorTemplate) {
                List<GemBonus> gemBonuses = new ArrayList<>();
                if (document.containsKey("gems")) {
                    loadGems(document, gemBonuses);
                }
                RunicItemArmorTemplate armorTemplate = (RunicItemArmorTemplate) template;
                return new RunicItemArmor(armorTemplate, count, id, loadStats(document, armorTemplate.getStats()), gemBonuses);
            } else if (template instanceof RunicItemArtifactTemplate) {
                RunicItemArtifactTemplate artifactTemplate = (RunicItemArtifactTemplate) template;
                return new RunicItemArtifact(artifactTemplate, count, id, loadStats(document, artifactTemplate.getStats()));
            } else if (template instanceof RunicItemBookTemplate) {
                RunicItemBookTemplate bookTemplate = (RunicItemBookTemplate) template;
                return new RunicItemBook(bookTemplate, count, id);
            } else if (template instanceof RunicItemDynamicTemplate) {
                int dynamicField = document.getInteger(RunicItemDynamic.getDynamicFieldString());
                return new RunicItemDynamic((RunicItemDynamicTemplate) template, count, id, dynamicField);
            } else if (template instanceof RunicItemGenericTemplate) {
                return new RunicItemGeneric((RunicItemGenericTemplate) template, count, id);
            } else if (template instanceof RunicItemOffhandTemplate) {
                RunicItemOffhandTemplate offhandTemplate = (RunicItemOffhandTemplate) template;
                return new RunicItemOffhand(offhandTemplate, count, id, loadStats(document, offhandTemplate.getStats()));
            } else if (template instanceof RunicItemWeaponTemplate) {
                RunicItemWeaponTemplate weaponTemplate = (RunicItemWeaponTemplate) template;
                return new RunicItemWeapon(weaponTemplate, count, id, loadStats(document, weaponTemplate.getStats()));
            } else if (template instanceof RunicItemGemTemplate) {
                RunicItemGemTemplate gemTemplate = (RunicItemGemTemplate) template;
                return new RunicItemGem(gemTemplate, count, id, new GemBonus(
                        loadGemStats(document),
                        document.containsKey("health") ? document.getInteger("health") : 0,
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
     * @param itemDataMap a map of key-value pairs representing the item's data
     * @param id          of the item for dupe manager
     * @return a RunicItem object
     */
    public static RunicItem loadItem(Map<String, String> itemDataMap, long id) {
        try {
            String templateId = itemDataMap.get("template-id");
            int count = Integer.parseInt(itemDataMap.get("count"));
            RunicItemTemplate template = TemplateManager.getTemplateFromId(templateId);
            if (template instanceof RunicItemArmorTemplate) {
                List<GemBonus> gemBonuses = getGemBonuses(itemDataMap);
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
            Bukkit.getLogger().log(Level.INFO, "[RunicItems] Error loading item " + itemDataMap.get("template-id") + " from redis!");
            exception.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * Loads the stats of an item from a mongo document
     *
     * @param document      of the runic item / inventory
     * @param templateStats the default stats map from the template
     * @return a sorted map of stats
     */
    @SuppressWarnings("unchecked")
    private static LinkedHashMap<Stat, RunicItemStat> loadStats(Document document, Map<Stat, RunicItemStatRange> templateStats) {
        Map<Stat, RunicItemStat> stats = new HashMap<>();
        if (document.containsKey("stats")) {
            Map<String, Double> documentStatMap = (Map<String, Double>) document.get("stats");
            Set<String> sectionKeys = documentStatMap.keySet();
            for (Stat templateStatType : templateStats.keySet()) {
                if (sectionKeys.contains(templateStatType.getIdentifier())) { // Item has stat and has already been rolled (stored in database)
                    stats.put(templateStatType, new RunicItemStat(templateStats.get(templateStatType), documentStatMap.get(templateStatType.getIdentifier())));
                } else { // Item has recently been added this stat and the roll does not exist in database, so make a new roll
                    stats.put(templateStatType, new RunicItemStat(templateStats.get(templateStatType)));
                }
            }
        }
        return StatUtil.sortStatMap(stats);
    }

    /**
     * Loads a gem from mongo
     *
     * @param document the document source for the RunicItem
     * @return a map of each gem stat and its value
     */
    @SuppressWarnings("unchecked")
    private static LinkedHashMap<Stat, Integer> loadGemStats(Document document) {
        Map<Stat, Integer> stats = new HashMap<>();
        if (document.containsKey("gem-stats")) {
            Map<String, Double> gemStatsMap = (Map<String, Double>) document.get("gem-stats");
            for (String key : gemStatsMap.keySet()) {
                Stat stat = Stat.getFromIdentifier(key);
                if (stat == null) continue;
                stats.put(stat, gemStatsMap.get(key).intValue());
            }
        }
        return StatUtil.sortStatMap(stats);
    }

    /**
     * Loads the gems in a piece of armor from mongo
     *
     * @param gemBonuses an empty list of gem bonuses to append
     * @return the mutated gem bonus list
     */
    @SuppressWarnings("unchecked")
    private static List<GemBonus> loadGems(Document document, List<GemBonus> gemBonuses) {
        Map<String, Map<String, String>> documentGemMap = (Map<String, Map<String, String>>) document.get("gems");
        for (String gemSlotKey : documentGemMap.keySet()) { // 0, 1, etc.

            LinkedHashMap<Stat, Integer> gemStats = new LinkedHashMap<>();
            for (String statKey : documentGemMap.get(gemSlotKey).keySet()) {
                if (!statKey.equalsIgnoreCase("health")
                        && !statKey.equalsIgnoreCase("main")
                        && !statKey.equalsIgnoreCase("tier")) {
                    gemStats.put(Stat.getFromIdentifier(statKey), Integer.valueOf(documentGemMap.get(gemSlotKey).get(statKey)));
                }
            }
            // Load gem health
            int health = 0;
            if (documentGemMap.get(gemSlotKey).get("health") != null) {
                health = Integer.parseInt(documentGemMap.get(gemSlotKey).get("health"));
            }
            // Determine main gem stat
            String mainStat = "";
            if (documentGemMap.get(gemSlotKey).get("main") != null) {
                mainStat = documentGemMap.get(gemSlotKey).get("main");
            }
            // Determine gem tier
            int tier = 0;
            if (documentGemMap.get(gemSlotKey).get("tier") != null) {
                tier = Integer.parseInt(documentGemMap.get(gemSlotKey).get("tier"));
            }
            gemBonuses.add(new GemBonus
                    (
                            gemStats,
                            health,
                            Stat.getFromIdentifier(mainStat),
                            tier
                    ));
        }

        return gemBonuses;
    }

    /**
     * Loads the stats of an item from redis
     *
     * @param itemDataMap   a map of key-value pairs from redis
     * @param templateStats the default stats map from the template
     * @return a sorted map of stats
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
     * Loads gem stats from redis
     *
     * @param itemDataMap a map of key-value pairs from redis
     * @return a map that can be applied to a runic item
     */
    private static LinkedHashMap<Stat, Integer> loadGemStats(Map<String, String> itemDataMap) {
        Map<Stat, Integer> stats = new HashMap<>();

        for (Stat stat : Stat.values()) { // for all POSSIBLE values of stat
            if (itemDataMap.containsKey("gem-stats:" + stat.getIdentifier())) { // Item has stat and has already been rolled (stored in database)
                int statValue = Integer.parseInt(itemDataMap.get("gem-stats:" + stat.getIdentifier()));
                stats.put(stat, statValue);
            }
        }
        return StatUtil.sortStatMap(stats);
    }

    /**
     * Runs through the keys obtained from redis and assigns gem bonuses based on the slot (gem key)
     *
     * @param itemDataMap from redis with key-value pairs
     * @return a list of gem bonuses to apply to the armor item
     */
    private static List<GemBonus> getGemBonuses(Map<String, String> itemDataMap) {
        List<GemBonus> gemBonuses = new ArrayList<>();
        for (int i = 0; i < MAX_GEM_SLOTS; i++) { // gems.0.main
            if (!itemDataMap.containsKey("gems." + i + ".main")) continue;
            final int gemSlot = i;
            LinkedHashMap<Stat, Integer> gemStats = new LinkedHashMap<>();
            for (String statKey : itemDataMap.keySet().stream().filter(s -> s.startsWith("gems." + gemSlot)).collect(Collectors.toList())) { // gem.0.main
                if (!statKey.equalsIgnoreCase("gems." + gemSlot + ".health")
                        && !statKey.equalsIgnoreCase("gems." + gemSlot + ".main")
                        && !statKey.equalsIgnoreCase("gems." + gemSlot + ".tier")) {
                    gemStats.put(Stat.getFromIdentifier(statKey.split("\\.")[2]), Integer.valueOf(itemDataMap.get(statKey)));
                }
            }
            gemBonuses.add(new GemBonus(
                    gemStats,
                    itemDataMap.containsKey("gems." + gemSlot + ".health") ? Integer.parseInt(itemDataMap.get("gems." + gemSlot + ".health")) : 0,
                    Stat.getFromIdentifier(itemDataMap.get("gems." + gemSlot + ".main")),
                    Integer.parseInt(itemDataMap.get("gems." + gemSlot + ".tier"))));
        }
        return gemBonuses;
    }

}
