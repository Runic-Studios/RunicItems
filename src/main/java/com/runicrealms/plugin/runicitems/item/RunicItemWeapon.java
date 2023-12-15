package com.runicrealms.plugin.runicitems.item;

import com.runicrealms.plugin.common.util.LazyField;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.runicitems.RunicItems;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.TemplateManager;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerk;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkType;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemStat;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;
import com.runicrealms.plugin.runicitems.item.util.ItemLoreBuilder;
import com.runicrealms.plugin.runicitems.item.util.RunicItemClass;
import com.runicrealms.plugin.runicitems.player.AddedStats;
import com.runicrealms.plugin.runicitems.weaponskin.WeaponSkin;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

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

public class RunicItemWeapon extends RunicItem implements AddedStatsHolder, ItemPerksHolder, ClassRequirementHolder, LevelRequirementHolder {

    private final int level;
    private final RunicItemRarity rarity;
    private final RunicItemClass runicClass;
    private final RunicItemStatRange damageRange;
    private final LinkedHashMap<Stat, RunicItemStat> stats;
    private final @Nullable WeaponSkin activeSkin;
    private final LinkedHashSet<ItemPerk> itemPerks;
    private final LazyField<AddedStats> addedStats;

    public RunicItemWeapon(
            String templateId,
            DisplayableItem displayableItem,
            List<RunicItemTag> tags, Map<String, String> data,
            int count,
            long id,
            RunicItemStatRange damageRange,
            LinkedHashMap<Stat, RunicItemStat> stats,
            LinkedHashSet<ItemPerk> itemPerks,
            int level,
            RunicItemRarity rarity,
            RunicItemClass runicClass,
            @Nullable WeaponSkin activeSkin) {
        super(templateId, displayableItem, tags, data, count, id);
        this.level = level;
        this.rarity = rarity;
        this.runicClass = runicClass;
        this.damageRange = damageRange;
        this.stats = stats;
        this.itemPerks = itemPerks;
        this.activeSkin = activeSkin;
        this.addedStats = new LazyField<>(() -> {
            LinkedHashMap<Stat, Integer> calculatedStats = new LinkedHashMap<>();
            for (Stat stat : stats.keySet()) {
                calculatedStats.put(stat, this.stats.get(stat).getValue());
            }
            return new AddedStats(calculatedStats, this.itemPerks, 0);
        });
    }

    public RunicItemWeapon(RunicItemWeaponTemplate template, int count, long id, LinkedHashMap<Stat, RunicItemStat> stats, LinkedHashSet<ItemPerk> itemPerks, @Nullable WeaponSkin activeSkin) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getDamageRange(), stats, itemPerks,
                template.getLevel(), template.getRarity(), template.getRunicClass(), activeSkin
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
        WeaponSkin skin = null;
        if (nbtItem.hasNBTData() && nbtItem.hasKey("weapon-skin")) {
            skin = RunicItems.getWeaponSkinAPI().getWeaponSkin(nbtItem.getString("weapon-skin"));
        }
        return new RunicItemWeapon(template, item.getAmount(), nbtItem.getInteger("id"), stats, perks, skin);
    }

    @Override
    public Map<String, String> addToRedis() {
        Map<String, String> jedisDataMap = super.addToRedis();
        for (Stat statType : this.stats.keySet()) {
            jedisDataMap.put("stats:" + statType.getIdentifier(), String.valueOf(this.stats.get(statType).getRollPercentage()));
        }
        if (this.activeSkin != null) {
            jedisDataMap.put("weapon-skin", this.activeSkin.id());
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
        meta.setDisplayName(this.getRarity().getChatColor() + this.getDisplayableItem().getDisplayName()); // apply rarity color
        item.setItemMeta(meta);
        NBTItem nbtItem = new NBTItem(item, true);
        int count = 0;
        for (Stat statType : this.stats.keySet()) {
            nbtItem.setDouble("stat-" + count + "-" + statType.getIdentifier(), this.stats.get(statType).getRollPercentage());
            count++;
        }
        if (this.activeSkin != null) {
            nbtItem.setString("weapon-skin", this.activeSkin.id());
            item = nbtItem.getItem();
            this.activeSkin.apply(item);
        }
        for (ItemPerk perk : this.itemPerks) {
            nbtItem.setInteger("perks-" + perk.getType().getIdentifier(), perk.getStacks());
        }
        return item;
    }

    @Override
    protected List<String> generateLore() {
        List<String> statLore = new LinkedList<>();
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
                .newLine()
                .appendLines(ChatColor.RED + "" + damageRange.getMin() + "-" + damageRange.getMax() + " DMG")
                .newLineIf(statLore.size() > 0)
                .appendLinesIf(statLore.size() > 0, statLore)
                .newLineIf(perkLore.size() > 0)
                .appendLines(perkLore)
                .newLine()
                .appendLines(rarity.getDisplay())
                .appendLinesIf(level > 0, "<level> " + ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level)
                .appendLines("<class> " + ChatColor.GRAY + runicClass.getDisplay())
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
        if (this.activeSkin != null) {
            document.put("weapon-skin", this.activeSkin.id());
        }
        Map<String, Integer> perksMap = new HashMap<>();
        for (ItemPerk perk : this.itemPerks) {
            perksMap.put(perk.getType().getIdentifier(), perk.getStacks());
        }
        if (!perksMap.isEmpty()) {
            document.put("perks", perksMap);
        }
        return document;
    }

    public RunicItemStatRange getDamageRange() {
        return damageRange;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

    @Override
    public RunicItemClass getRunicClass() {
        return this.runicClass;
    }

    public LinkedHashMap<Stat, RunicItemStat> getStats() {
        return stats;
    }

    public RunicItemStatRange getWeaponDamage() {
        return this.damageRange;
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
