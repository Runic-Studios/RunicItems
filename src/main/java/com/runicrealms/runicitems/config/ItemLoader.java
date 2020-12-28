package com.runicrealms.runicitems.config;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.RunicItemArmor;
import com.runicrealms.runicitems.item.RunicItemArtifact;
import com.runicrealms.runicitems.item.RunicItemBook;
import com.runicrealms.runicitems.item.RunicItemGeneric;
import com.runicrealms.runicitems.item.RunicItemOffhand;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import com.runicrealms.runicitems.item.inventory.*;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemLoader {

    public static RunicItem loadItem(Data section) {
        try {
            String templateId = section.get("template-id", String.class);
            int count = section.get("count", Integer.class);
            long id = section.get("item-id", Long.class);
            RunicItemOwner itemOwner = getItemOwnerFromSection(section.getSection("owner"));
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
                return new RunicItemArmor(armorTemplate, count, id, itemOwner, loadStats(section, armorTemplate.getStats()), gems);
            } else if (template instanceof RunicItemArtifactTemplate) {
                RunicItemArtifactTemplate artifactTemplate = (RunicItemArtifactTemplate) template;
                return new RunicItemArtifact(artifactTemplate, count, id, itemOwner, loadStats(section, artifactTemplate.getStats()));
            } else if (template instanceof RunicItemBookTemplate) {
                RunicItemBookTemplate bookTemplate = (RunicItemBookTemplate) template;
                return new RunicItemBook(bookTemplate, count, id, itemOwner);
            } else if (template instanceof RunicItemGenericTemplate) {
                return new RunicItemGeneric((RunicItemGenericTemplate) template, count, id, itemOwner);
            } else if (template instanceof RunicItemOffhandTemplate) {
                RunicItemOffhandTemplate offhandTemplate = (RunicItemOffhandTemplate) template;
                return new RunicItemOffhand(offhandTemplate, count, id, itemOwner, loadStats(section, offhandTemplate.getStats()));
            } else if (template instanceof RunicItemWeaponTemplate) {
                RunicItemWeaponTemplate weaponTemplate = (RunicItemWeaponTemplate) template;
                return new RunicItemWeapon(weaponTemplate, count, id, itemOwner, loadStats(section, weaponTemplate.getStats()));
            }
        } catch (
                Exception exception) {
            return null;
        }
        return null;
    }

    private static LinkedHashMap<RunicItemStatType, RunicItemStat> loadStats(Data section, Map<RunicItemStatType, RunicItemStatRange> templateStats) {
        LinkedHashMap<RunicItemStatType, RunicItemStat> stats = new LinkedHashMap<RunicItemStatType, RunicItemStat>();
        Set<String> sectionKeys = section.getSection("stats").getKeys();
        for (RunicItemStatType templateStatType : templateStats.keySet()) {
            if (sectionKeys.contains(templateStatType.getIdentifier())) { // Item has stat and has already been rolled (stored in database)
                stats.put(templateStatType, new RunicItemStat(templateStats.get(templateStatType), section.get("stats." + templateStatType.getIdentifier(), Float.class)));
            } else { // Item has recently been added this stat and the roll does not exist in database, so make a new roll
                stats.put(templateStatType, new RunicItemStat(templateStats.get(templateStatType)));
            }
        }
        return stats;
    }

    private static RunicItemOwner getItemOwnerFromSection(Data section) {
        switch (RunicInventory.getFromIdentifier(section.get("inventory", String.class))) {
            case PLAYER_INVENTORY:
                return new RunicItemOwnerPlayerInventory(section.get("identifier", String.class));
            case PLAYER_BANK:
                return new RunicItemOwnerPlayerBank(section.get("identifier", String.class));
            case GUILD_BANK:
                return new RunicItemOwnerGuildBank(section.get("identifier", String.class));
            case TRADE_MARKET:
                return new RunicItemOwnerTradeMarket(section.get("identifier", String.class));
            case DROPPED:
                return new RunicItemOwnerDropped(section.get("identifier", String.class));
            default:
                return null;
        }
    }

}
