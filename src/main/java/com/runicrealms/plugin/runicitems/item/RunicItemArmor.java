package com.runicrealms.plugin.runicitems.item;

import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.TemplateManager;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerk;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkHandler;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkType;
import com.runicrealms.plugin.runicitems.item.stats.GemBonus;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemStat;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;
import com.runicrealms.plugin.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.plugin.runicitems.item.util.DisplayableItem;
import com.runicrealms.plugin.runicitems.item.util.ItemLoreSection;
import com.runicrealms.plugin.runicitems.item.util.RunicItemClass;
import com.runicrealms.plugin.runicitems.player.AddedStats;
import com.runicrealms.plugin.runicitems.util.LazyField;
import com.runicrealms.plugin.runicitems.util.StatUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
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

public class RunicItemArmor extends RunicItem implements AddedStatsHolder, ItemPerksHolder, ClassRequirementHolder, LevelRequirementHolder {

    private static final AttributeModifier attributeModifier = new AttributeModifier("generic.armor", 0, AttributeModifier.Operation.ADD_NUMBER);

    private final int level;
    private final RunicItemRarity rarity;
    private final int health;
    private final LinkedHashMap<Stat, RunicItemStat> stats;
    private final List<GemBonus> gemBonuses;
    private final int maxGemSlots;
    private final LinkedHashSet<ItemPerk> itemPerks;
    private final RunicItemClass runicClass;
    private final LazyField<AddedStats> addedStats;

    public RunicItemArmor(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                          int health, LinkedHashMap<Stat, RunicItemStat> stats, List<GemBonus> gemBonuses, int maxGemSlots, LinkedHashSet<ItemPerk> itemPerks,
                          int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(templateId, displayableItem, tags, data, count, id);
        this.rarity = rarity;
        this.level = level;
        this.health = health;
        this.gemBonuses = gemBonuses;
        this.itemPerks = itemPerks;
        this.stats = stats;
        this.maxGemSlots = maxGemSlots;
        this.runicClass = runicClass;
        this.addedStats = new LazyField<>(() -> {
            LinkedHashMap<Stat, Integer> calculatedStats = new LinkedHashMap<>();
            for (Stat stat : stats.keySet()) {
                calculatedStats.put(stat, this.stats.get(stat).getValue());
            }
            int bonusHealth = this.health;
            for (GemBonus gemBonus : this.gemBonuses) {
                for (Stat stat : gemBonus.getStats().keySet()) {
                    if (!calculatedStats.containsKey(stat)) calculatedStats.put(stat, 0);
                    calculatedStats.put(stat, calculatedStats.get(stat) + gemBonus.getStats().get(stat));
                }
                bonusHealth += gemBonus.getHealth();
            }
            return new AddedStats(calculatedStats, this.itemPerks, bonusHealth);
        });
    }


    public RunicItemArmor(RunicItemArmorTemplate template, int count, long id, LinkedHashMap<Stat, RunicItemStat> stats, List<GemBonus> gemBonuses, LinkedHashSet<ItemPerk> itemPerks) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getHealth(), stats, gemBonuses, template.getMaxGemSlots(), itemPerks,
                template.getLevel(), template.getRarity(), template.getRunicClass()
        );
    }

    public static RunicItemArmor getFromItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemArmorTemplate template))
            throw new IllegalArgumentException("ItemStack is not an armor item!");
        Set<String> keys = nbtItem.getKeys();
        int amountOfStats = 0;
        for (String key : keys) {
            if (key.startsWith("stat")) {
                amountOfStats++;
            }
        }

        List<Pair<Stat, RunicItemStat>> statsList = new ArrayList<>(amountOfStats);
        Map<Integer, LinkedHashMap<Stat, Integer>> gemStats = new HashMap<>();
        Map<Integer, Integer> gemHealth = new HashMap<>();
        Map<Integer, Stat> gemMainStat = new HashMap<>();
        Map<Integer, Integer> gemTier = new HashMap<>();
        LinkedHashSet<ItemPerk> perks = new LinkedHashSet<>();

        for (int i = 0; i < amountOfStats; i++) {
            statsList.add(null);
        }
        for (String key : keys) {
            String[] split = key.split("-");
            if (split[0].equals("stat") && split.length >= 3) {
                Stat statType = Stat.getFromIdentifier(split[2]);
                RunicItemStat stat = new RunicItemStat(template.getStats().get(statType), nbtItem.getDouble(key));
                statsList.set(Integer.parseInt(split[1]), new Pair<>(statType, stat));
            } else if (split[0].equals("gem") && split.length >= 3) {

                int gemNumber = Integer.parseInt(split[1]);
                if (!gemStats.containsKey(gemNumber))
                    gemStats.put(gemNumber, new LinkedHashMap<>());

                String statName = split[2];
                if (statName.equalsIgnoreCase("health")) {
                    gemHealth.put(gemNumber, nbtItem.getInteger(key));
                } else if (statName.equalsIgnoreCase("main")) {
                    gemMainStat.put(gemNumber, Stat.getFromIdentifier(nbtItem.getString(key)));
                } else if (statName.equalsIgnoreCase("tier")) {
                    gemTier.put(gemNumber, nbtItem.getInteger(key));
                } else {
                    gemStats.get(gemNumber).put(Stat.getFromIdentifier(split[2]), nbtItem.getInteger(key));
                }
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

        List<GemBonus> gemBonuses = new ArrayList<>();
        for (Integer gemNumber : gemStats.keySet()) {
            gemBonuses
                    .add(new GemBonus(
                            gemStats.get(gemNumber),
                            gemHealth.getOrDefault(gemNumber, 0),
                            gemMainStat.get(gemNumber),
                            gemTier.get(gemNumber)));
        }

        return new RunicItemArmor(template, item.getAmount(), nbtItem.getInteger("id"), stats, gemBonuses, perks);
    }

    @Override
    public Map<String, String> addToRedis() {
        Map<String, String> jedisDataMap = super.addToRedis();
        for (Stat statType : this.stats.keySet()) {
            jedisDataMap.put("stats:" + statType.getIdentifier(), String.valueOf(this.stats.get(statType).getRollPercentage()));
        }
        int count = 0;
        for (GemBonus gemBonus : this.gemBonuses) {
            for (Stat statType : gemBonus.getStats().keySet()) {
                jedisDataMap.put("gems." + count + "." + statType.getIdentifier(), String.valueOf(gemBonus.getStats().get(statType)));
            }
            if (gemBonus.getHealth() != 0)
                jedisDataMap.put("gems." + count + ".health", String.valueOf(gemBonus.getHealth()));
            jedisDataMap.put("gems." + count + ".tier", String.valueOf(gemBonus.getTier()));
            jedisDataMap.put("gems." + count + ".main", gemBonus.getMainStat().getIdentifier());
            count++;
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
        meta.addAttributeModifier(Attribute.GENERIC_ARMOR, attributeModifier);
        item.setItemMeta(meta);
        NBTItem nbtItem = new NBTItem(item, true);
        int count = 0;
        for (Stat statType : this.stats.keySet()) {
            nbtItem.setDouble("stat-" + count + "-" + statType.getIdentifier(), this.stats.get(statType).getRollPercentage());
            count++;
        }
        count = 0;
        for (GemBonus gemBonus : this.gemBonuses) {
            for (Stat statType : gemBonus.getStats().keySet()) {
                nbtItem.setInteger("gem-" + count + "-" + statType.getIdentifier(), gemBonus.getStats().get(statType));
            }
            if (gemBonus.getHealth() != 0)
                nbtItem.setInteger("gem-" + count + "-health", gemBonus.getHealth());
            nbtItem.setString("gem-" + count + "-main", gemBonus.getMainStat().getIdentifier());
            nbtItem.setInteger("gem-" + count + "-tier", gemBonus.getTier());
            count++;
        }
        for (ItemPerk perk : this.itemPerks) {
            nbtItem.setInteger("perks-" + perk.getType().getIdentifier(), perk.getStacks());
        }
        return item;
    }

    @Override
    protected ItemLoreSection[] generateLore() {
        List<String> statLore = new LinkedList<>();

        Map<Stat, Integer> gemOnlyStats = new HashMap<>();
        for (GemBonus gemBonus : gemBonuses) {
            for (Stat gemStat : gemBonus.getStats().keySet()) {
                if (stats.containsKey(gemStat)) continue;
                if (!gemOnlyStats.containsKey(gemStat)) gemOnlyStats.put(gemStat, 0);
                gemOnlyStats.put(gemStat, gemOnlyStats.get(gemStat) + gemBonus.getStats().get(gemStat));
            }
        }

        for (Stat stat : Stat.values()) {
            if (stats.get(stat) != null && stats.get(stat).getValue() == 0) continue;
            if (isMenuDisplay && stats.containsKey(stat)) {
                statLore.add(stat.getChatColor() + "+" + stats.get(stat).getRange().getMin() +
                        "-" + stats.get(stat).getRange().getMax() + stat.getIcon());
            } else if (stats.containsKey(stat)) {
                int value = stats.get(stat).getValue();
                int finalValue = value;
                for (GemBonus gemBonus : gemBonuses) {
                    if (gemBonus.getStats().containsKey(stat)) {
                        finalValue += gemBonus.getStats().get(stat);
                    }
                }
                if (finalValue == value) {
                    statLore.add(stat.getChatColor()
                            + (value < 0 ? "-" : "+")
                            + value
                            + stat.getIcon());
                } else {
                    statLore.add("" + ChatColor.GRAY + ChatColor.STRIKETHROUGH
                            + (value < 0 ? "-" : "+")
                            + value + (stat.getIcon().length() == 1 ? stat.getIcon() : "") + ChatColor.RESET + " "
                            + stat.getChatColor()
                            + (finalValue < 0 ? "-" : "+")
                            + finalValue
                            + stat.getIcon()
                    );
                }

            } else if (gemOnlyStats.containsKey(stat)) {
                int value = gemOnlyStats.get(stat);
                statLore.add("" + ChatColor.GRAY + ChatColor.STRIKETHROUGH
                        + "+0" + (stat.getIcon().length() == 1 ? stat.getIcon() : "") + ChatColor.RESET + " "
                        + stat.getChatColor()
                        + (value < 0 ? "-" : "+")
                        + value
                        + stat.getIcon());
            }
        }

        int finalHealth = health;
        for (GemBonus gemBonus : gemBonuses)
            if (gemBonus.hasHealth()) finalHealth += gemBonus.getHealth();
        String healthString;
        if (finalHealth == health) {
            healthString = ChatColor.RED + "" + health + Stat.HEALTH_ICON;
        } else {
            healthString = "" + ChatColor.GRAY + ChatColor.STRIKETHROUGH + health + Stat.HEALTH_ICON + ChatColor.RESET + " " + ChatColor.RED + finalHealth + Stat.HEALTH_ICON;
        }

        StringBuilder gemTextBuilder = new StringBuilder(ChatColor.GRAY.toString())
                .append("Gem Slots: ")
                .append(ChatColor.WHITE)
                .append("[ ");
        int counter = 0;
        for (GemBonus gemBonus : gemBonuses) {
            for (int i = 0; i < StatUtil.getGemSlots(gemBonus.getTier()); i++) {
                gemTextBuilder.append(gemBonus.getMainStat().getChatColor()).append(gemBonus.getMainStat().getIcon()).append(" ");
                counter++;
            }
        }
        gemTextBuilder.append(ChatColor.GRAY);
        for (int i = counter; i < maxGemSlots; i++) {
            gemTextBuilder.append(Stat.EMPTY_GEM_ICON).append(" ");
        }
        gemTextBuilder.append(ChatColor.WHITE).append("]");

        LinkedList<String> perkLore = new LinkedList<>();
        boolean atLeastOnePerk = false;
        for (ItemPerk perk : this.itemPerks) {
            ItemPerkHandler handler = RunicItemsAPI.getItemPerkManager().getHandler(perk.getType());
            perkLore.add(ChatColor.translateAlternateColorCodes('&',
                    "&7[&r<"
                            + handler.getDynamicItemPerksStacksTextPlaceholder().getIdentifier()
                            + ">&7/"
                            + perk.getType().getMaxStacks()
                            + "] &f+" + perk.getStacks()
                            + " &r" + handler.getName()));
            List<String> handlerLore = handler.getLoreSection();
            if (handlerLore != null) perkLore.addAll(handlerLore);
            perkLore.add("");
            atLeastOnePerk = true;
        }
        if (atLeastOnePerk) perkLore.removeLast();

        String levelString = level > 0 ? String.valueOf(level) : "None";
        return new ItemLoreSection[]{
                (maxGemSlots > 0
                        ? new ItemLoreSection(new String[]{
                        "<level> " + ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + levelString,
                        gemTextBuilder.toString()})
                        : new ItemLoreSection(new String[]{
                        "<level> " + ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + levelString,
                })),
                new ItemLoreSection(new String[]{healthString}),
                new ItemLoreSection(statLore),
                new ItemLoreSection(perkLore),
                new ItemLoreSection(new String[]{
                        rarity.getDisplay() + " " + getArmorName(),
                        "<class> " + ChatColor.GRAY + runicClass.getDisplay()
                }),
        };
    }

    @Override
    public Document writeToDocument(RunicItem source, Document document) {
        document = super.writeToDocument(source, document);
        Map<String, Double> statsMap = new HashMap<>();
        for (Stat statType : this.stats.keySet()) {
            statsMap.put(statType.getIdentifier(), this.stats.get(statType).getRollPercentage());
        }
        document.put("stats", statsMap);
        int count = 0;
        Map<String, Map<String, String>> gemStatsMap = new HashMap<>();

        for (GemBonus gemBonus : this.gemBonuses) {
            gemStatsMap.put(String.valueOf(count), new HashMap<>());
            for (Stat statType : gemBonus.getStats().keySet()) {
                gemStatsMap.get(String.valueOf(count)).put(statType.getIdentifier(), String.valueOf(gemBonus.getStats().get(statType)));
            }
            if (gemBonus.getHealth() != 0)
                gemStatsMap.get(String.valueOf(count)).put("health", String.valueOf(gemBonus.getHealth()));
            gemStatsMap.get(String.valueOf(count)).put("tier", String.valueOf(gemBonus.getTier()));
            gemStatsMap.get(String.valueOf(count)).put("main", gemBonus.getMainStat().getIdentifier());
            count++;
        }
        if (!gemStatsMap.isEmpty()) {
            document.put("gems", gemStatsMap);
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

    @Override
    public AddedStats getAddedStats() {
        return this.addedStats.get();
    }

    public List<GemBonus> getGems() {
        return this.gemBonuses;
    }

    public int getHealth() {
        return this.health;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    public int getMaxGemSlots() {
        return this.maxGemSlots;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

    @Override
    public RunicItemClass getRunicClass() {
        return this.runicClass;
    }

    public LinkedHashMap<Stat, RunicItemStat> getStats() {
        return this.stats;
    }

    @Override
    public LinkedHashSet<ItemPerk> getItemPerks() {
        return this.itemPerks;
    }

    public boolean hasItemPerks() {
        return this.itemPerks != null && this.itemPerks.size() > 0;
    }

    private String getArmorName() {
        String materialName = this.displayableItem.getMaterial().name();
        if (materialName.contains("HELMET")) return "Helmet";
        if (materialName.contains("CHESTPLATE")) return "Chestplate";
        if (materialName.contains("LEGGINGS")) return "Leggings";
        if (materialName.contains("BOOTS")) return "Boots";
        return "";
    }

}
