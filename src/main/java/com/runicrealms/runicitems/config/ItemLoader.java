package com.runicrealms.runicitems.config;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.AbilityManager;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.RunicItemArmor;
import com.runicrealms.runicitems.item.RunicItemArtifact;
import com.runicrealms.runicitems.item.RunicItemGeneric;
import com.runicrealms.runicitems.item.RunicItemOffhand;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.runicitems.item.template.RunicItemArtifactTemplate;
import com.runicrealms.runicitems.item.template.RunicItemGenericTemplate;
import com.runicrealms.runicitems.item.template.RunicItemOffhandTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.template.RunicItemWeaponTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemLoader {

    public static RunicItem loadItem(Data section) {
        String id = section.get("item-id", String.class);
        int count = section.get("count", Integer.class);
        RunicItemTemplate template = TemplateManager.getTemplateFromId(id);
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
            return new RunicItemArmor(armorTemplate, count, loadStats(section, armorTemplate.getStats()), gems);
        } else if (template instanceof RunicItemArtifactTemplate) {
            RunicItemArtifactTemplate artifactTemplate = (RunicItemArtifactTemplate) template;
            return new RunicItemArtifact(artifactTemplate, count, loadStats(section, artifactTemplate.getStats()));
        } else if (template instanceof RunicItemGenericTemplate) {
            return new RunicItemGeneric((RunicItemGenericTemplate) template, count);
        } else if (template instanceof RunicItemOffhandTemplate) {
            RunicItemOffhandTemplate offhandTemplate = (RunicItemOffhandTemplate) template;
            return new RunicItemOffhand(offhandTemplate, count, loadStats(section, offhandTemplate.getStats()));
        } else if (template instanceof RunicItemWeaponTemplate) {
            RunicItemWeaponTemplate weaponTemplate = (RunicItemWeaponTemplate) template;
            return new RunicItemWeapon(weaponTemplate, count, loadStats(section, weaponTemplate.getStats()));
        }
        return null;
    }

    private static LinkedHashMap<RunicItemStatType, RunicItemStat> loadStats(Data section, Map<RunicItemStatType, RunicItemStatRange> templateStats) {
        LinkedHashMap<RunicItemStatType, RunicItemStat> stats = new LinkedHashMap<RunicItemStatType, RunicItemStat>();
        for (String key : section.getSection("stats").getKeys()) {
            RunicItemStatType statType = RunicItemStatType.getFromIdentifier(key);
            stats.put(statType, new RunicItemStat(templateStats.get(statType), section.get("stats." + key, Float.class)));
        }
        return stats;
    }

}
