package com.runicrealms.plugin.runicitems.config;

import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.TemplateManager;
import com.runicrealms.plugin.runicitems.item.RunicItemDynamic;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;
import com.runicrealms.plugin.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemBookTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemDynamicTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemGemTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemGenericTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemOffhandTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.plugin.runicitems.item.util.ClickTrigger;
import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;
import com.runicrealms.plugin.runicitems.item.util.RunicItemClass;
import com.runicrealms.plugin.runicitems.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

public class TemplateLoader {

    private static void addConfigs(File file, Collection<ConfigurationSection> existingConfigs) {
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            if (config.isConfigurationSection("items")) {
                for (String key : config.getConfigurationSection("items").getKeys(false)) {
                    existingConfigs.add(config.getConfigurationSection("items." + key));
                }
            } else {
                existingConfigs.add(config);
            }
        } catch (Exception exception) {
            Bukkit.getLogger().log(Level.SEVERE, "[RunicItems] WARNING: failed to load items file " + file.getName());
        }
    }

    public static void loadTemplates() {
        Map<String, RunicItemTemplate> templates = new HashMap<>();
        Set<ConfigurationSection> itemSections = new HashSet<>();

        for (File file : Objects.requireNonNull(new File(RunicItems.getInstance().getDataFolder(), "items").listFiles())) {
            addConfigs(file, itemSections);
        }

        File scriptFile = new File(RunicItems.getInstance().getDataFolder(), "script-items.yml");
        if (scriptFile.exists()) {
            addConfigs(scriptFile, itemSections);
        } else {
            for (File file : Objects.requireNonNull(new File(RunicItems.getInstance().getDataFolder(), "script").listFiles())) {
                addConfigs(file, itemSections);
            }
        }
        for (ConfigurationSection section : itemSections) {
            // Bukkit.getLogger().log(Level.INFO, "[RunicItems] Loading template " + file.getName()); // for debugging
            try {
                RunicItemTemplate template = loadTemplate(section);
                if (template == null) {
                    Bukkit.getLogger().log(Level.SEVERE, "[RunicItems] WARNING: failed to load template " + section.getName());
                    continue;
                }
                templates.put(template.getId(), template);
            } catch (Exception exception) {
                Bukkit.getLogger().log(Level.SEVERE, "[RunicItems] Error loading template: " + section.getName());
                exception.printStackTrace();
            }
        }
        TemplateManager.setTemplates(templates);
    }

    public static RunicItemTemplate loadTemplate(ConfigurationSection itemConfig) {
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
        Map<String, String> data = new HashMap<>();
        if (itemConfig.contains("data")) {
            for (String key : itemConfig.getConfigurationSection("data").getKeys(false)) {
                data.put(key, itemConfig.getString("data." + key));
            }
        }
        if (itemConfig.getString("type").equalsIgnoreCase("armor")) {
            return new RunicItemArmorTemplate(
                    id, displayableItem, tags, data,
                    itemConfig.getInt("health"), loadStats(itemConfig), itemConfig.getInt("max-gem-slots"),
                    itemConfig.getInt("level"), RunicItemRarity.getFromIdentifier(itemConfig.getString("rarity")), RunicItemClass.getFromIdentifier(itemConfig.getString("class"))
            );
        } else if (itemConfig.getString("type").equalsIgnoreCase(RunicItemDynamic.getDynamicFieldString())) {
            return new RunicItemDynamicTemplate(
                    id, displayableItem, tags, data, loadTriggers(itemConfig),
                    itemConfig.getStringList("lore"), itemConfig.getInt(RunicItemDynamic.getDynamicFieldString())
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
                    itemConfig.getStringList("lore"),
                    itemConfig.contains("author") ? itemConfig.getString("author") : null,
                    itemConfig.getStringList("pages")
            );
        } else if (itemConfig.getString("type").equalsIgnoreCase("gem")) {
            Stat mainStat = Stat.getFromIdentifier(itemConfig.getString("main-stat"));
            if (mainStat != null) return new RunicItemGemTemplate(
                    id, displayableItem, tags, data,
                    itemConfig.getInt("tier"), mainStat
            );
        }
        return null;
    }

    private static RunicItemStatRange loadDamage(ConfigurationSection itemConfig) {
        return new RunicItemStatRange(itemConfig.getInt("damage.min"), itemConfig.getInt("damage.max"));
    }

    private static LinkedHashMap<Stat, RunicItemStatRange> loadStats(ConfigurationSection itemConfig) {
        if (itemConfig.contains("stats")) {
            Map<Stat, RunicItemStatRange> stats = new HashMap<>();
            for (String key : itemConfig.getConfigurationSection("stats").getKeys(false)) {
                if (Stat.getFromIdentifier(key) == null) continue; // typos or removed stats
                stats.put(Stat.getFromIdentifier(key), new RunicItemStatRange(
                        itemConfig.getInt("stats." + key + ".min"),
                        itemConfig.getInt("stats." + key + ".max")
                ));
            }
            ;
            return StatUtil.sortStatMap(stats);
        }
        return new LinkedHashMap<>();
    }

    private static Map<ClickTrigger, String> loadTriggers(ConfigurationSection itemConfig) {
        Map<ClickTrigger, String> triggers = new HashMap<>();
        if (itemConfig.contains("triggers")) {
            for (String key : itemConfig.getConfigurationSection("triggers").getKeys(false)) {
                triggers.put(ClickTrigger.getFromIdentifier(key), itemConfig.getString("triggers." + key));
            }
        }
        return triggers;
    }
}
