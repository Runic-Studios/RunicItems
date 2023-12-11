package com.runicrealms.plugin.runicitems.item;

import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.TemplateManager;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemStat;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;
import com.runicrealms.plugin.runicitems.item.template.RunicItemOffhandTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;
import com.runicrealms.plugin.runicitems.item.util.ItemLoreSection;
import com.runicrealms.plugin.runicitems.player.AddedStats;
import com.runicrealms.plugin.runicitems.util.LazyField;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RunicItemOffhand extends RunicItem implements AddedStatsHolder, LevelRequirementHolder {

    private final LinkedHashMap<Stat, RunicItemStat> stats;
    private final int level;
    private final RunicItemRarity rarity;
    private final LazyField<AddedStats> addedStats;

    public RunicItemOffhand(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                            LinkedHashMap<Stat, RunicItemStat> stats,
                            int level, RunicItemRarity rarity) {
        super(templateId, displayableItem, tags, data, count, id);
        this.stats = stats;
        this.level = level;
        this.rarity = rarity;
        this.addedStats = new LazyField<>(() -> {
            Map<Stat, Integer> addedStats = new HashMap<>();
            for (Stat stat : stats.keySet()) {
                addedStats.put(stat, stats.get(stat).getValue());
            }
            return new AddedStats(addedStats, null, 0);
        });
    }

    public RunicItemOffhand(RunicItemOffhandTemplate template, int count, long id, LinkedHashMap<Stat, RunicItemStat> stats) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                stats,
                template.getLevel(), template.getRarity()
        );
    }

    public static RunicItemOffhand getFromItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemOffhandTemplate))
            throw new IllegalArgumentException("ItemStack is not an offhand item!");
        RunicItemOffhandTemplate template = (RunicItemOffhandTemplate) uncastedTemplate;
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
        return new RunicItemOffhand(template, item.getAmount(), nbtItem.getInteger("id"), stats);
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
        if (meta != null) {
            meta.setDisplayName(this.getRarity().getChatColor() + this.getDisplayableItem().getDisplayName()); // apply rarity color
            item.setItemMeta(meta);
        }
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
                (level > 0 ? new ItemLoreSection(new String[]{"<level> " + ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level}) : new ItemLoreSection(new String[]{""})),
                new ItemLoreSection(lore),
                new ItemLoreSection(Collections.singletonList(rarity.getDisplay())),
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

    @Override
    public int getLevel() {
        return this.level;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

    public LinkedHashMap<Stat, RunicItemStat> getStats() {
        return this.stats;
    }

    @Override
    public AddedStats getAddedStats() {
        return this.addedStats.get();
    }
}
