package com.runicrealms.runicitems.config;

import com.runicrealms.runicitems.Plugin;
import com.runicrealms.runicitems.AbilityManager;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.exception.InvalidTemplateException;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.runicitems.item.template.RunicItemArtifactTemplate;
import com.runicrealms.runicitems.item.template.RunicItemBookTemplate;
import com.runicrealms.runicitems.item.template.RunicItemGenericTemplate;
import com.runicrealms.runicitems.item.template.RunicItemOffhandTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.RunicItemClass;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TemplateLoader {

    public static void loadTemplates() throws InvalidTemplateException {
        File folder = new File(Plugin.getInstance().getDataFolder(), "items");
        Map<String, RunicItemTemplate> templates = new HashMap<String, RunicItemTemplate>();
        for (File file : folder.listFiles()) {

            try {
                FileConfiguration itemConfig = ConfigUtil.getYamlConfigFile(file.getName(), Plugin.getInstance().getDataFolder());
                RunicItemTemplate template;
                template = loadTemplate(itemConfig);
                if (template == null) throw new InvalidTemplateException("Error in template: " + file.getName());
                templates.put(template.getId(), template);
            } catch (Exception exception) {
                exception.printStackTrace();
                throw new InvalidTemplateException("Error in template: " + file.getName());
            }

        }
        TemplateManager.setTemplates(templates);
    }

    public static RunicItemTemplate loadTemplate(FileConfiguration itemConfig) {
        String id = itemConfig.getString("id");
        List<RunicItemTag> tags = new ArrayList<RunicItemTag>();
        if (itemConfig.contains("tags")) {
            for (String tag : itemConfig.getStringList("tags")) {
                tags.add(RunicItemTag.getFromIdentifier(tag));
            }
        }
        DisplayableItem displayableItem = new DisplayableItem(
                itemConfig.getString("display.name"),
                Material.getMaterial(itemConfig.getString("display.material")),
                itemConfig.contains("display.damage") ? (short) itemConfig.getInt("display.damage") : 0
        );
        Map<String, Object> data = new HashMap<String, Object>();
        if (itemConfig.contains("data")) {
            for (String key : itemConfig.getConfigurationSection("data").getKeys(false)) {
                data.put(key, itemConfig.get("data." + key));
            }
        }
        if (itemConfig.getString("type").equalsIgnoreCase("armor")) {
            return new RunicItemArmorTemplate(
                    id, displayableItem, tags, data,
                    loadStats(itemConfig), itemConfig.getInt("max-gem-slots"),
                    itemConfig.getInt("level"), RunicItemRarity.getFromIdentifier(itemConfig.getString("rarity")), RunicItemClass.getFromIdentifier(itemConfig.getString("class"))
            );
        } else if (itemConfig.getString("type").equalsIgnoreCase("artifact")) {
            return new RunicItemArtifactTemplate(
                    id, displayableItem, tags, data,
                    AbilityManager.getAbilityFromId(itemConfig.getString("ability")), loadDamage(itemConfig), loadStats(itemConfig),
                    itemConfig.getInt("level"), RunicItemRarity.getFromIdentifier(itemConfig.getString("rarity")), RunicItemClass.getFromIdentifier(itemConfig.getString("class"))
            );
        } else if (itemConfig.getString("type").equalsIgnoreCase("generic")) {
            return new RunicItemGenericTemplate(
                    id, displayableItem, tags, data, loadTriggers(itemConfig),
                    itemConfig.getStringList("lore")
            );
        } else if (itemConfig.getString("type").equalsIgnoreCase("offhand")) {
            return new RunicItemOffhandTemplate(
                    id, displayableItem, tags, data,
                    loadStats(itemConfig),
                    itemConfig.getInt("level"), RunicItemRarity.getFromIdentifier(itemConfig.getString("rarity"))
            );
        } else if (itemConfig.getString("type").equalsIgnoreCase("weapon")) {
            return new RunicItemWeaponTemplate(
                    id, displayableItem, tags, data,
                    loadDamage(itemConfig), loadStats(itemConfig),
                    itemConfig.getInt("level"), RunicItemRarity.getFromIdentifier(itemConfig.getString("rarity")), RunicItemClass.getFromIdentifier(itemConfig.getString("class"))
            );
        } else if (itemConfig.getString("type").equalsIgnoreCase("book")) {
            return new RunicItemBookTemplate(
                    id, displayableItem, tags, data,
                    itemConfig.getStringList("lore"), itemConfig.getString("author"), itemConfig.getStringList("pages")
            );
        } else {
            return null;
        }
    }

    private static RunicItemStatRange loadDamage(FileConfiguration itemConfig) {
        return new RunicItemStatRange(itemConfig.getInt("damage.min"), itemConfig.getInt("damage.max"));
    }

    private static LinkedHashMap<RunicItemStatType, RunicItemStatRange> loadStats(FileConfiguration itemConfig) {
        LinkedHashMap<RunicItemStatType, RunicItemStatRange> stats = new LinkedHashMap<RunicItemStatType, RunicItemStatRange>();
        for (String key : itemConfig.getConfigurationSection("stats").getKeys(false)) {
            stats.put(RunicItemStatType.getFromIdentifier(key), new RunicItemStatRange(
                    itemConfig.getInt("stats." + key + ".min"),
                    itemConfig.getInt("stats." + key + ".max")
            ));
        }
        return stats;
    }

    private static Map<ClickTrigger, String> loadTriggers(FileConfiguration itemConfig) {
        Map<ClickTrigger, String> triggers = new HashMap<ClickTrigger, String>();
        if (itemConfig.contains("triggers")) {
            for (String key : itemConfig.getConfigurationSection("triggers").getKeys(false)) {
                triggers.put(ClickTrigger.getFromIdentifier(key), itemConfig.getString("triggers." + key));
            }
        }
        return triggers;
    }
}
