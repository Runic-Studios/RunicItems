package com.runicrealms.runicitems.config;

import com.runicrealms.runicitems.Plugin;
import com.runicrealms.runicitems.SpellManager;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.runicitems.item.template.RunicItemArtifactTemplate;
import com.runicrealms.runicitems.item.template.RunicItemGenericTemplate;
import com.runicrealms.runicitems.item.template.RunicItemOffhandTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.runicitems.item.util.DefaultSpell;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.RunicItemClass;
import com.runicrealms.runicitems.item.util.ClickTrigger;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TemplateLoader {

    public static void loadTemplates() {
        File folder = new File(Plugin.getInstance().getDataFolder(), "items");
        Map<String, RunicItemTemplate> templates = new HashMap<String, RunicItemTemplate>();
        for (File file : folder.listFiles()) {
            FileConfiguration itemConfig = YamlConfiguration.loadConfiguration(file);
            RunicItemTemplate template = loadTemplate(itemConfig);
            if (template == null) throw new NullPointerException();
            templates.put(template.getId(), template);
        }
        TemplateManager.setTemplates(templates);
    }

    public static RunicItemTemplate loadTemplate(FileConfiguration itemConfig) {
        String id = itemConfig.getString("id");
        List<RunicItemTag> tags = new ArrayList<RunicItemTag>();
        if (itemConfig.contains("tags")) {
            for (String tag : itemConfig.getStringList("tags")) {
                for (RunicItemTag target : RunicItemTag.values()) {
                    if (target.getIdentifier().equalsIgnoreCase(tag)) {
                        tags.add(target);
                        break;
                    }
                }
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
                    itemConfig.getInt("level"), loadRarity(itemConfig), loadClass(itemConfig)
            );
        } else if (itemConfig.getString("type").equalsIgnoreCase("artifact")) {
            return new RunicItemArtifactTemplate(
                    id, displayableItem, tags, data,
                    new DefaultSpell(
                            loadSpellClickTrigger(itemConfig, "default-spell"),
                            SpellManager.getSpellFromId(itemConfig.getString("default-spell.spell"))
                    ), loadDamage(itemConfig),
                    itemConfig.getInt("level"), loadRarity(itemConfig), loadClass(itemConfig)
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
                    itemConfig.getInt("level"), loadRarity(itemConfig)
            );
        } else if (itemConfig.getString("type").equalsIgnoreCase("weapon")) {
            return new RunicItemWeaponTemplate(
                    id, displayableItem, tags, data,
                    loadDamage(itemConfig),
                    itemConfig.getInt("level"), loadRarity(itemConfig), loadClass(itemConfig)
            );
        } else {
            return null;
        }
    }

    private static RunicItemRarity loadRarity(FileConfiguration itemConfig) {
        for (RunicItemRarity target : RunicItemRarity.values()) {
            if (target.getIdentifier().equalsIgnoreCase(itemConfig.getString("rarity"))) {
                return target;
            }
        }
        throw new NullPointerException();
    }

    private static RunicItemStatRange loadDamage(FileConfiguration itemConfig) {
        return new RunicItemStatRange(itemConfig.getInt("damage.min"), itemConfig.getInt("damage.max"));
    }

    private static LinkedHashMap<RunicItemStatType, RunicItemStatRange> loadStats(FileConfiguration itemConfig) {
        LinkedHashMap<RunicItemStatType, RunicItemStatRange> stats = new LinkedHashMap<RunicItemStatType, RunicItemStatRange>();
        for (String key : itemConfig.getConfigurationSection("stats").getKeys(false)) {
            for (RunicItemStatType target : RunicItemStatType.values()) {
                if (target.getIdentifier().equalsIgnoreCase(key)) {
                    stats.put(target, new RunicItemStatRange(
                            itemConfig.getInt("stats." + key + ".min"),
                            itemConfig.getInt("stats." + key + ".max")
                    ));
                    break;
                }
            }
        }
        return stats;
    }

    private static ClickTrigger loadSpellClickTrigger(FileConfiguration itemConfig, String key) {
        for (ClickTrigger target : ClickTrigger.values()) {
            if (target.getIdentifier().equalsIgnoreCase(itemConfig.getString(key + ".click"))) {
                return target;
            }
        }
        throw new NullPointerException();
    }

    private static RunicItemClass loadClass(FileConfiguration itemConfig) {
        for (RunicItemClass target : RunicItemClass.values()) {
            if (target.getIdentifier().equalsIgnoreCase(itemConfig.getString("class"))) {
                return target;
            }
        }
        throw new NullPointerException();
    }

    private static Map<ClickTrigger, String> loadTriggers(FileConfiguration itemConfig) {
        Map<ClickTrigger, String> triggers = new HashMap<ClickTrigger, String>();
        if (itemConfig.contains("triggers")) {
            for (String key : itemConfig.getConfigurationSection("triggers").getKeys(false)) {
                for (ClickTrigger target : ClickTrigger.values()) {
                    if (target.getIdentifier().equalsIgnoreCase(key)) {
                        triggers.put(target, itemConfig.getString("triggers." + key));
                        break;
                    }
                }
            }
        }
        return triggers;
    }
}
