package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.api.Pair;
import com.runicrealms.runicitems.Stat;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.GemBonus;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.RunicItemClass;
import com.runicrealms.runicitems.player.AddedArmorStats;
import com.runicrealms.runicitems.util.StatUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RunicItemArmor extends RunicItem {

    private static final AttributeModifier attributeModifier = new AttributeModifier("generic.armor", 0, AttributeModifier.Operation.ADD_NUMBER);

    private final int level;
    private final RunicItemRarity rarity;
    private final int health;
    private final LinkedHashMap<Stat, RunicItemStat> stats;
    private final List<GemBonus> gemBonuses;
    private final int maxGemSlots;
    private final RunicItemClass runicClass;

    public RunicItemArmor(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                          int health, LinkedHashMap<Stat, RunicItemStat> stats, List<GemBonus> gemBonuses, int maxGemSlots,
                          int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(templateId, displayableItem, tags, data, count, id);
        this.rarity = rarity;
        this.level = level;
        this.health = health;
        this.gemBonuses = gemBonuses;
        this.stats = stats;
        this.maxGemSlots = maxGemSlots;
        this.runicClass = runicClass;
    }


    public RunicItemArmor(RunicItemArmorTemplate template, int count, long id, LinkedHashMap<Stat, RunicItemStat> stats, List<GemBonus> gemBonuses) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getHealth(), stats, gemBonuses, template.getMaxGemSlots(),
                template.getLevel(), template.getRarity(), template.getRunicClass()
        );
    }

    public static RunicItemArmor getFromItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemArmorTemplate))
            throw new IllegalArgumentException("ItemStack is not an armor item!");
        RunicItemArmorTemplate template = (RunicItemArmorTemplate) uncastedTemplate;
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
                RunicItemStat stat = new RunicItemStat(template.getStats().get(statType), nbtItem.getDouble(key));
                statsList.set(Integer.parseInt(split[1]), new Pair<>(statType, stat));
            }
        }
        LinkedHashMap<Stat, RunicItemStat> stats = new LinkedHashMap<>();
        for (Pair<Stat, RunicItemStat> stat : statsList) {
            stats.put(stat.first, stat.second);
        }

        Map<Integer, LinkedHashMap<Stat, Integer>> gemStats = new HashMap<>();
        Map<Integer, Integer> gemHealth = new HashMap<>();
        Map<Integer, Stat> gemMainStat = new HashMap<>();
        Map<Integer, Integer> gemTier = new HashMap<>();

        for (String key : keys) {
            String[] split = key.split("-");
            if (split[0].equals("gem")) {
                ;

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
            }
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

        return new RunicItemArmor(template, item.getAmount(), nbtItem.getInteger("id"), stats, gemBonuses);
    }

    @Override
    public Map<String, String> addToJedis() {
        Map<String, String> jedisDataMap = super.addToJedis();
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
        return item;
    }

    @Override
    protected ItemLoreSection[] generateLore() {
        List<String> lore = new LinkedList<>();

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
                lore.add(stat.getChatColor() + "+" + stats.get(stat).getRange().getMin() +
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
                    lore.add(stat.getChatColor()
                            + (value < 0 ? "-" : "+")
                            + value
                            + stat.getIcon());
                } else {
                    lore.add("" + ChatColor.GRAY + ChatColor.STRIKETHROUGH
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
                lore.add("" + ChatColor.GRAY + ChatColor.STRIKETHROUGH
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

        if (level > 0) {
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
            return new ItemLoreSection[]{
                    (maxGemSlots > 0
                            ? new ItemLoreSection(new String[]{
                            ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level,
                            gemTextBuilder.toString()})
                            : new ItemLoreSection(new String[]{
                            ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level,
                    })),
                    new ItemLoreSection(new String[]{healthString}),
                    new ItemLoreSection(lore),
                    new ItemLoreSection(new String[]{
                            rarity.getDisplay(),
                            ChatColor.GRAY + runicClass.getDisplay()
                    }),
            };
        } else {
            return new ItemLoreSection[]{
                    new ItemLoreSection(new String[]{healthString}),
                    new ItemLoreSection(lore),
                    new ItemLoreSection(new String[]{rarity.getDisplay(), ChatColor.GRAY + runicClass.getDisplay()}),
            };
        }
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
        return document;
    }

    public AddedArmorStats calculateAddedStats() {
        LinkedHashMap<Stat, Integer> calculatedStats = new LinkedHashMap<>();
        int health = this.health;
        for (Stat stat : this.stats.keySet()) {
            calculatedStats.put(stat, this.stats.get(stat).getValue());
        }
        for (GemBonus gemBonus : this.gemBonuses) {
            for (Stat stat : gemBonus.getStats().keySet()) {
                if (!calculatedStats.containsKey(stat)) calculatedStats.put(stat, 0);
                calculatedStats.put(stat, calculatedStats.get(stat) + gemBonus.getStats().get(stat));
            }
            health += gemBonus.getHealth();
        }
        return new AddedArmorStats(calculatedStats, health);
    }

    public List<GemBonus> getGems() {
        return this.gemBonuses;
    }

    public int getHealth() {
        return this.health;
    }

    public int getLevel() {
        return this.level;
    }

    public int getMaxGemSlots() {
        return this.maxGemSlots;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

    public RunicItemClass getRunicClass() {
        return this.runicClass;
    }

    public LinkedHashMap<Stat, RunicItemStat> getStats() {
        return this.stats;
    }

}
