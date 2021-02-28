package com.runicrealms.runicitems.config;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.RunicItemArmor;
import com.runicrealms.runicitems.item.RunicItemArtifact;
import com.runicrealms.runicitems.item.RunicItemBook;
import com.runicrealms.runicitems.item.RunicItemGeneric;
import com.runicrealms.runicitems.item.RunicItemOffhand;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.runicitems.item.template.RunicItemArtifactTemplate;
import com.runicrealms.runicitems.item.template.RunicItemBookTemplate;
import com.runicrealms.runicitems.item.template.RunicItemGenericTemplate;
import com.runicrealms.runicitems.item.template.RunicItemOffhandTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.template.RunicItemWeaponTemplate;
import org.bukkit.Bukkit;

import java.util.ArrayList;
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
                List<LinkedHashMap<RunicItemStatType, Integer>> gems = new ArrayList<LinkedHashMap<RunicItemStatType, Integer>>();
                if (section.has("gems")) {
                    for (String gemKey : section.getSection("gems").getKeys()) {
                        LinkedHashMap<RunicItemStatType, Integer> gem = new LinkedHashMap<RunicItemStatType, Integer>();
                        for (String statKey : section.getSection("gems." + gemKey).getKeys()) {
                            gem.put(RunicItemStatType.getFromIdentifier(statKey), section.get("gems." + gemKey + "." + statKey, Integer.class));
                        }
                        gems.add(gem);
                    }
                }
                RunicItemArmorTemplate armorTemplate = (RunicItemArmorTemplate) template;
                return new RunicItemArmor(armorTemplate, count, id, loadStats(section, armorTemplate.getStats()), gems);
            } else if (template instanceof RunicItemArtifactTemplate) {
                RunicItemArtifactTemplate artifactTemplate = (RunicItemArtifactTemplate) template;
                RunicItemArtifact item = new RunicItemArtifact(artifactTemplate, count, id, loadStats(section, artifactTemplate.getStats()));
                return item;
            } else if (template instanceof RunicItemBookTemplate) {
                RunicItemBookTemplate bookTemplate = (RunicItemBookTemplate) template;
                RunicItemBook item = new RunicItemBook(bookTemplate, count, id);
                return item;
            } else if (template instanceof RunicItemGenericTemplate) {
                RunicItemGeneric item = new RunicItemGeneric((RunicItemGenericTemplate) template, count, id);
                return item;
            } else if (template instanceof RunicItemOffhandTemplate) {
                RunicItemOffhandTemplate offhandTemplate = (RunicItemOffhandTemplate) template;
                RunicItemOffhand item = new RunicItemOffhand(offhandTemplate, count, id, loadStats(section, offhandTemplate.getStats()));
                return item;
            } else if (template instanceof RunicItemWeaponTemplate) {
                RunicItemWeaponTemplate weaponTemplate = (RunicItemWeaponTemplate) template;
                RunicItemWeapon item = new RunicItemWeapon(weaponTemplate, count, id, loadStats(section, weaponTemplate.getStats()));
                return item;
            }
        } catch (Exception exception) {
            Bukkit.getLogger().log(Level.INFO, "[RunicItems] Error loading item!");
            exception.printStackTrace();
            return null;
        }
        return null;
    }

    private static LinkedHashMap<RunicItemStatType, RunicItemStat> loadStats(Data section, Map<RunicItemStatType, RunicItemStatRange> templateStats) {
        LinkedHashMap<RunicItemStatType, RunicItemStat> stats = new LinkedHashMap<RunicItemStatType, RunicItemStat>();
        if (section.has("stats")) {
            Set<String> sectionKeys = section.getSection("stats").getKeys();
            for (RunicItemStatType templateStatType : templateStats.keySet()) {
                if (sectionKeys.contains(templateStatType.getIdentifier())) { // Item has stat and has already been rolled (stored in database)
                    stats.put(templateStatType, new RunicItemStat(templateStats.get(templateStatType), section.get("stats." + templateStatType.getIdentifier(), Float.class)));
                } else { // Item has recently been added this stat and the roll does not exist in database, so make a new roll
                    stats.put(templateStatType, new RunicItemStat(templateStats.get(templateStatType)));
                }
            }
        }
        return stats;
    }

}
