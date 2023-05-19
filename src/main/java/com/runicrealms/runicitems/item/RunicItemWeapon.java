package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.api.Pair;
import com.runicrealms.runicitems.Stat;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.RunicItemClass;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Document(collection = "items")
@TypeAlias("weapon")
public class RunicItemWeapon extends RunicItem {
    protected int level;
    protected RunicItemRarity rarity;
    protected RunicItemClass runicClass;
    protected RunicItemStatRange damageRange;
    protected LinkedHashMap<Stat, RunicItemStat> stats;

    @SuppressWarnings("unused")
    public RunicItemWeapon() {
        super();
        // Default constructor for Spring
    }

    @Autowired
    public RunicItemWeapon(
            String templateId,
            DisplayableItem displayableItem,
            List<RunicItemTag> tags, Map<String, String> data,
            int count,
            long id,
            RunicItemStatRange damageRange,
            LinkedHashMap<Stat, RunicItemStat> stats,
            int level,
            RunicItemRarity rarity,
            RunicItemClass runicClass) {
        super(templateId, displayableItem, tags, data, count, id);
        this.level = level;
        this.rarity = rarity;
        this.runicClass = runicClass;
        this.damageRange = damageRange;
        this.stats = stats;
    }

    public RunicItemWeapon(RunicItemWeaponTemplate template, int count, long id, LinkedHashMap<Stat, RunicItemStat> stats) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getDamageRange(), stats,
                template.getLevel(), template.getRarity(), template.getRunicClass()
        );
    }

    public static RunicItemWeapon getFromItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemWeaponTemplate))
            throw new IllegalArgumentException("ItemStack is not a weapon item!");
        RunicItemWeaponTemplate template = (RunicItemWeaponTemplate) uncastedTemplate;
        Set<String> keys = nbtItem.getKeys();
        int amountOfStats = 0;
        for (String key : keys) {
            if (key.startsWith("stat")) {
                amountOfStats++;
            }
        }
        List<Pair<Stat, RunicItemStat>> statsList = new ArrayList<>(amountOfStats);
        for (int i = 0; i < amountOfStats; i++) {
            statsList.add(null);
        }
        for (String key : keys) {
            String[] split = key.split("-");
            if (split[0].equals("stat")) {
                Stat statType = Stat.getFromIdentifier(split[2]);
                RunicItemStat stat = new RunicItemStat(template.getStats().get(statType), nbtItem.getFloat(key));
                statsList.set(Integer.parseInt(split[1]), new Pair<>(statType, stat));
            }
        }
        LinkedHashMap<Stat, RunicItemStat> stats = new LinkedHashMap<>();
        for (Pair<Stat, RunicItemStat> stat : statsList) {
            stats.put(stat.first, stat.second);
        }
        return new RunicItemWeapon(template, item.getAmount(), nbtItem.getInteger("id"), stats);
    }

    @Override
    public Map<String, String> addToRedis() {
        Map<String, String> jedisDataMap = super.addToRedis();
        for (Stat statType : this.stats.keySet()) {
            jedisDataMap.put("stats:" + statType.getIdentifier(), String.valueOf(this.stats.get(statType).getRollPercentage()));
        }
        return jedisDataMap;
    }

    @Override
    public ItemStack generateItem() {
        ItemStack item = super.generateItem();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(this.getRarity().getChatColor() + this.getDisplayableItem().getDisplayName()); // apply rarity color
        item.setItemMeta(meta);
        NBTItem nbtItem = new NBTItem(item, true);
        int count = 0;
        for (Stat statType : this.stats.keySet()) {
            nbtItem.setDouble("stat-" + count + "-" + statType.getIdentifier(), this.stats.get(statType).getRollPercentage());
            count++;
        }
        return item;
    }

    @Override
    protected ItemLoreSection[] generateLore() {
        List<String> lore = new LinkedList<>();
        for (Map.Entry<Stat, RunicItemStat> entry : stats.entrySet()) {
            lore.add(
                    entry.getKey().getChatColor()
                            + (entry.getValue().getValue() < 0 ? "-" : "+")
                            + entry.getValue().getValue()
                            + entry.getKey().getIcon()
            );
        }
        return new ItemLoreSection[]{
                (level > 0 ? new ItemLoreSection(new String[]{ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level}) : new ItemLoreSection(new String[]{""})),
                new ItemLoreSection(new String[]{ChatColor.RED + "" + damageRange.getMin() + "-" + damageRange.getMax() + " DMG"}),
                new ItemLoreSection(lore),
                new ItemLoreSection(new String[]{rarity.getDisplay(), ChatColor.GRAY + runicClass.getDisplay()}),
        };
    }

    @Override
    public org.bson.Document writeToDocument(RunicItem source, org.bson.Document document) {
        document = super.writeToDocument(source, document);
        Map<String, Double> statsMap = new HashMap<>();
        for (Stat statType : this.stats.keySet()) {
            statsMap.put(statType.getIdentifier(), this.stats.get(statType).getRollPercentage());
        }
        document.put("stats", statsMap);
        return document;
    }

    public RunicItemStatRange getDamageRange() {
        return damageRange;
    }

    public void setDamageRange(RunicItemStatRange damageRange) {
        this.damageRange = damageRange;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

    public void setRarity(RunicItemRarity rarity) {
        this.rarity = rarity;
    }

    public RunicItemClass getRunicClass() {
        return this.runicClass;
    }

    public void setRunicClass(RunicItemClass runicClass) {
        this.runicClass = runicClass;
    }

    public LinkedHashMap<Stat, RunicItemStat> getStats() {
        return stats;
    }

    public void setStats(LinkedHashMap<Stat, RunicItemStat> stats) {
        this.stats = stats;
    }

    public RunicItemStatRange getWeaponDamage() {
        return this.damageRange;
    }

}
