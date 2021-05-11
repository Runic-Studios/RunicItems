package com.runicrealms.runicitems.config;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.Stat;
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
                List<LinkedHashMap<Stat, Integer>> gems = new ArrayList<>();
                if (section.has("gems")) {
                    for (String gemKey : section.getSection("gems").getKeys()) {
                        LinkedHashMap<Stat, Integer> gem = new LinkedHashMap<>();
                        for (String statKey : section.getSection("gems." + gemKey).getKeys()) {
                            gem.put(Stat.getFromName(statKey), section.get("gems." + gemKey + "." + statKey, Integer.class));
                        }
                        gems.add(gem);
                    }
                }
                RunicItemArmorTemplate armorTemplate = (RunicItemArmorTemplate) template;
                return new RunicItemArmor(armorTemplate, count, id, loadStats(section, armorTemplate.getStats()), gems);
            } else if (template instanceof RunicItemArtifactTemplate) {
                RunicItemArtifactTemplate artifactTemplate = (RunicItemArtifactTemplate) template;
                return new RunicItemArtifact(artifactTemplate, count, id, loadStats(section, artifactTemplate.getStats()));
            } else if (template instanceof RunicItemBookTemplate) {
                RunicItemBookTemplate bookTemplate = (RunicItemBookTemplate) template;
                return new RunicItemBook(bookTemplate, count, id);
            } else if (template instanceof RunicItemGenericTemplate) {
                return new RunicItemGeneric((RunicItemGenericTemplate) template, count, id);
            } else if (template instanceof RunicItemOffhandTemplate) {
                RunicItemOffhandTemplate offhandTemplate = (RunicItemOffhandTemplate) template;
                return new RunicItemOffhand(offhandTemplate, count, id, loadStats(section, offhandTemplate.getStats()));
            } else if (template instanceof RunicItemWeaponTemplate) {
                RunicItemWeaponTemplate weaponTemplate = (RunicItemWeaponTemplate) template;
                return new RunicItemWeapon(weaponTemplate, count, id, loadStats(section, weaponTemplate.getStats()));
            }
        } catch (Exception exception) {
            Bukkit.getLogger().log(Level.INFO, "[RunicItems] Error loading item!");
            exception.printStackTrace();
            return null;
        }
        return null;
    }

    private static LinkedHashMap<Stat, RunicItemStat> loadStats(Data section, Map<Stat, RunicItemStatRange> templateStats) {
        LinkedHashMap<Stat, RunicItemStat> stats = new LinkedHashMap<>();
        if (section.has("stats")) {
            Set<String> sectionKeys = section.getSection("stats").getKeys();
            for (Stat templateStatType : templateStats.keySet()) {
                if (sectionKeys.contains(templateStatType.getName())) { // Item has stat and has already been rolled (stored in database)
                    stats.put(templateStatType, new RunicItemStat(templateStats.get(templateStatType), section.get("stats." + templateStatType.getName(), Double.class)));
                } else { // Item has recently been added this stat and the roll does not exist in database, so make a new roll
                    stats.put(templateStatType, new RunicItemStat(templateStats.get(templateStatType)));
                }
            }
        }
        return stats;
    }

}
