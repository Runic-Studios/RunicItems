package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.Stat;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.Gem;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.RunicItemClass;
import com.runicrealms.runicitems.player.AddedArmorStats;
import de.tr7zw.nbtapi.NBTItem;
import javafx.util.Pair;
import org.bukkit.ChatColor;
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
    private final List<Gem> gems;
    private final int maxGemSlots;
    private final RunicItemClass runicClass;

    public RunicItemArmor(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                          int health, LinkedHashMap<Stat, RunicItemStat> stats, List<Gem> gems, int maxGemSlots,
                          int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(templateId, displayableItem, tags, data, count, id, () -> {
            List<String> lore = new ArrayList<>();

            Map<Stat, Integer> gemOnlyStats = new HashMap<>();
            for (Gem gem : gems) {
                for (Stat gemStat : gem.getStats().keySet()) {
                    if (stats.containsKey(gemStat)) continue;
                    if (!gemOnlyStats.containsKey(gemStat)) gemOnlyStats.put(gemStat, 0);
                    gemOnlyStats.put(gemStat, gemOnlyStats.get(gemStat) + gem.getStats().get(gemStat));
                }
            }

            for (Stat stat : Stat.values()) {
                if (stats.containsKey(stat)) {

                    int value = stats.get(stat).getValue();
                    if (value == 0) continue;
                    int finalValue = value;
                    for (Gem gem : gems) {
                        if (gem.getStats().containsKey(stat)) {
                            finalValue += gem.getStats().get(stat);
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
                                + value + stat.getIcon() + ChatColor.RESET + " "
                                + stat.getChatColor()
                                + (finalValue < 0 ? "-" : "+")
                                + finalValue
                                + stat.getIcon()
                        );
                    }

                } else if (gemOnlyStats.containsKey(stat)) {
                    int value = gemOnlyStats.get(stat);
                    lore.add("" + ChatColor.GRAY + ChatColor.STRIKETHROUGH
                            + "+0" + stat.getIcon() + ChatColor.RESET + " "
                            + stat.getChatColor()
                            + (value < 0 ? "-" : "+")
                            + value
                            + stat.getIcon());
                }
            }

            int finalHealth = health;
            for (Gem gem : gems) if (gem.hasHealth()) finalHealth += gem.getHealth();
            String healthString;
            if (finalHealth == health) {
                healthString = ChatColor.RED + "" + health + Stat.HEALTH_ICON;
            } else {
                healthString = "" + ChatColor.GRAY + ChatColor.STRIKETHROUGH + health + Stat.HEALTH_ICON + ChatColor.RESET + " " + ChatColor.RED + finalHealth + Stat.HEALTH_ICON;
            }

            if (level > 0) {
                return new ItemLoreSection[]{
                        (maxGemSlots > 0
                                ? new ItemLoreSection(new String[] {
                                ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level,
                                ChatColor.GRAY + "[" + ChatColor.WHITE + gems.size() + ChatColor.GRAY + "/" + ChatColor.WHITE + maxGemSlots + ChatColor.GRAY + "] Gems",})
                                : new ItemLoreSection(new String[]{
                                ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level,
                        })),
                        new ItemLoreSection(new String[] {healthString}),
                        new ItemLoreSection(lore),
                        new ItemLoreSection(new String[] {
                                rarity.getDisplay(),
                                ChatColor.GRAY + runicClass.getDisplay()
                        }),
                };
            } else {
                return new ItemLoreSection[] {
                        new ItemLoreSection(new String[] {healthString}),
                        new ItemLoreSection(lore),
                        new ItemLoreSection(new String[] {
                                rarity.getDisplay(),
                                ChatColor.GRAY + runicClass.getDisplay()
                        }),
                };
            }
        });
        this.rarity = rarity;
        this.level = level;
        this.health = health;
        this.gems = gems;
        this.stats = stats;
        this.maxGemSlots = maxGemSlots;
        this.runicClass = runicClass;
    }

    public RunicItemArmor(RunicItemArmorTemplate template, int count, long id, LinkedHashMap<Stat, RunicItemStat> stats, List<Gem> gems) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getHealth(), stats, gems, template.getMaxGemSlots(),
                template.getLevel(), template.getRarity(), template.getRunicClass()
        );
    }

    public AddedArmorStats calculateAddedStats() {
        LinkedHashMap<Stat, Integer> calculatedStats = new LinkedHashMap<>();
        int health = this.health;
        for (Stat stat : this.stats.keySet()) {
            calculatedStats.put(stat, this.stats.get(stat).getValue());
        }
        for (Gem gem : this.gems) {
            for (Stat stat : gem.getStats().keySet()) {
                if (!calculatedStats.containsKey(stat)) calculatedStats.put(stat, 0);
                calculatedStats.put(stat, calculatedStats.get(stat) + gem.getStats().get(stat));
            }
            health += gem.getHealth();
        }
        return new AddedArmorStats(calculatedStats, health);
    }

    public int getHealth() {
        return this.health;
    }

    public LinkedHashMap<Stat, RunicItemStat> getStats() {
        return this.stats;
    }

    public List<Gem> getGems() {
        return this.gems;
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

    @Override
    public void addToData(Data section, String root) {
        super.addToData(section, root);
        for (Stat statType : this.stats.keySet()) {
            section.set(ItemManager.getInventoryPath() + "." + root + ".stats." + statType.getIdentifier(), this.stats.get(statType).getRollPercentage());
        }
        int count = 0;
        for (Gem gem : this.gems) {
            for (Stat statType : gem.getStats().keySet()) {
                section.set(ItemManager.getInventoryPath() + "." + root + ".gems." + count + "." + statType.toString().toLowerCase(), gem.getStats().get(statType));
            }
            section.set(ItemManager.getInventoryPath() + "." + root + ".gems." + count + ".health", gem.getHealth());
            count++;
        }
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
        for (Gem gem : this.gems) {
            for (Stat statType : this.stats.keySet()) {
                nbtItem.setInteger("gem-" + count + "-" + statType.getIdentifier(), gem.getStats().get(statType));
            }
            nbtItem.setInteger("gem-" + count + "-health", gem.getHealth());
            count++;
        }
        return item;
    }

    public static RunicItemArmor getFromItemStack(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemArmorTemplate)) throw new IllegalArgumentException("ItemStack is not an armor item!");
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
            stats.put(stat.getKey(), stat.getValue());
        }

        List<Gem> gems = new ArrayList<>();

        for (String key : keys) {
            String[] split = key.split("-");
            if (split[0].equals("gem")) {;

                int gemNumber = Integer.parseInt(split[1]);

                while (gems.size() <= gemNumber) {
                    gems.add(new Gem(new LinkedHashMap<>(), 0));
                }

                String statName = split[2];
                if (statName.equalsIgnoreCase("health")) {
                    gems.get(gemNumber).setHealth(nbtItem.getInteger(key));
                } else {
                    gems.get(gemNumber).getStats().put(Stat.getFromIdentifier(split[2]), nbtItem.getInteger(key));
                }
            }
        }

        return new RunicItemArmor(template, item.getAmount(), nbtItem.getInteger("id"), stats, gems);
    }

}
