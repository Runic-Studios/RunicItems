package com.runicrealms.plugin.runicitems.item;

import com.runicrealms.plugin.common.util.LazyField;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.TemplateManager;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerk;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkType;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemStat;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;
import com.runicrealms.plugin.runicitems.item.template.RunicItemOffhandTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;
import com.runicrealms.plugin.runicitems.item.util.ItemLoreBuilder;
import com.runicrealms.plugin.runicitems.player.AddedStats;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RunicItemOffhand extends RunicItem implements AddedStatsHolder, ItemPerksHolder, LevelRequirementHolder {

    private final LinkedHashMap<Stat, RunicItemStat> stats;
    private final int level;
    private final RunicItemRarity rarity;
    private final LazyField<AddedStats> addedStats;
    private final LinkedHashSet<ItemPerk> itemPerks;

    public RunicItemOffhand(
            String templateId,
            DisplayableItem displayableItem,
            List<RunicItemTag> tags,
            Map<String, String> data,
            int count,
            long id,
            LinkedHashMap<Stat, RunicItemStat> stats,
            LinkedHashSet<ItemPerk> itemPerks,
            int level,
            RunicItemRarity rarity) {
        super(templateId, displayableItem, tags, data, count, id);
        this.stats = stats;
        this.itemPerks = itemPerks;
        this.level = level;
        this.rarity = rarity;
        this.addedStats = new LazyField<>(() -> {
            Map<Stat, Integer> addedStats = new HashMap<>();
            for (Stat stat : stats.keySet()) {
                addedStats.put(stat, stats.get(stat).getValue());
            }
            return new AddedStats(addedStats, this.itemPerks, 0);
        });
    }

    public RunicItemOffhand(RunicItemOffhandTemplate template, int count, long id, LinkedHashMap<Stat, RunicItemStat> stats, LinkedHashSet<ItemPerk> itemPerks) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                stats, itemPerks,
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
        LinkedHashSet<ItemPerk> perks = new LinkedHashSet<>();
        for (String key : keys) {
            String[] split = key.split("-");
            if (split[0].equals("stat")) {
                Stat statType = Stat.getFromIdentifier(split[2]);
                RunicItemStat stat = new RunicItemStat(template.getStats().get(statType), nbtItem.getFloat(key));
                statsList.set(Integer.parseInt(split[1]), new Pair<>(statType, stat));
            } else if (split[0].equals("perks") && split.length >= 2) {
                String identifier = Arrays.stream(split, 1, split.length).collect(Collectors.joining("-"));
                for (ItemPerkType type : RunicItemsAPI.getItemPerkManager().getItemPerks()) {
                    if (type.getIdentifier().equalsIgnoreCase(identifier)) {
                        perks.add(new ItemPerk(type, nbtItem.getInteger(key)));
                        break;
                    }
                }
            }
        }
        LinkedHashMap<Stat, RunicItemStat> stats = new LinkedHashMap<>();
        for (Pair<Stat, RunicItemStat> stat : statsList) {
            stats.put(stat.first, stat.second);
        }
        return new RunicItemOffhand(template, item.getAmount(), nbtItem.getInteger("id"), stats, perks);
    }

    @Override
    public Map<String, String> addToRedis() {
        Map<String, String> jedisDataMap = super.addToRedis();
        for (Stat statType : this.stats.keySet()) {
            jedisDataMap.put("stats:" + statType.getIdentifier(), String.valueOf(this.stats.get(statType).getRollPercentage()));
        }
        for (ItemPerk perk : this.itemPerks) {
            jedisDataMap.put("perks." + perk.getType().getIdentifier(), String.valueOf(perk.getStacks()));
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
        for (ItemPerk perk : this.itemPerks) {
            nbtItem.setInteger("perks-" + perk.getType().getIdentifier(), perk.getStacks());
        }
        return item;
    }

    @Override
    protected List<String> generateLore() {
        LinkedList<String> statLore = new LinkedList<>();
        for (Map.Entry<Stat, RunicItemStat> entry : stats.entrySet()) {
            statLore.add(
                    entry.getKey().getChatColor()
                            + (entry.getValue().getValue() < 0 ? "-" : "+")
                            + entry.getValue().getValue()
                            + entry.getKey().getIcon()
            );
        }

        LinkedList<String> perkLore = new LinkedList<>();
        boolean atLeastOnePerk = false;
        for (ItemPerk perk : this.itemPerks) {
            ItemPerkHandler handler = RunicItemsAPI.getItemPerkManager().getHandler(perk.getType());
            perkLore.add("<" + handler.getDynamicItemPerksStacksTextPlaceholder().getIdentifier() + ">"
                    + ChatColor.WHITE
                    + "+" + perk.getStacks()
                    + " " + ChatColor.RESET + handler.getName());
            List<String> handlerLore = handler.getLoreSection();
            if (handlerLore != null) perkLore.addAll(handlerLore);
            perkLore.add("");
            atLeastOnePerk = true;
        }
        if (atLeastOnePerk) perkLore.removeLast();

        return new ItemLoreBuilder()
                .newLineIf(statLore.size() > 0)
                .appendLinesIf(statLore.size() > 0, statLore)
                .newLineIf(perkLore.size() > 0)
                .appendLines(perkLore)
                .newLine()
                .appendLines(rarity.getDisplay())
                .appendLinesIf(level > 0, "<level> " + ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level)
                .build();
    }

    @Override
    public org.bson.Document writeToDocument(RunicItem source, org.bson.Document document) {
        document = super.writeToDocument(source, document);
        Map<String, Double> statsMap = new HashMap<>();
        for (Stat statType : this.stats.keySet()) {
            statsMap.put(statType.getIdentifier(), this.stats.get(statType).getRollPercentage());
        }
        document.put("stats", statsMap);
        Map<String, Integer> perksMap = new HashMap<>();
        for (ItemPerk perk : this.itemPerks) {
            perksMap.put(perk.getType().getIdentifier(), perk.getStacks());
        }
        if (!perksMap.isEmpty()) {
            document.put("perks", perksMap);
        }
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
    public LinkedHashSet<ItemPerk> getItemPerks() {
        return this.itemPerks;
    }

    @Override
    public boolean hasItemPerks() {
        return this.itemPerks != null && this.itemPerks.size() > 0;
    }

    @Override
    public AddedStats getAddedStats() {
        return this.addedStats.get();
    }
}
