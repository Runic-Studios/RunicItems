package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.plugin.player.stat.PlayerStatEnum;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.RunicItemClass;
import de.tr7zw.nbtapi.NBTItem;
import javafx.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RunicItemArmor extends RunicItem {

    private final int level;
    private final RunicItemRarity rarity;
    private final int health;
    private final LinkedHashMap<PlayerStatEnum, RunicItemStat> stats;
    private final List<LinkedHashMap<PlayerStatEnum, Integer>> gems;
    private final int maxGemSlots;
    private final RunicItemClass runicClass;

    public RunicItemArmor(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                          int health, LinkedHashMap<PlayerStatEnum, RunicItemStat> stats, List<LinkedHashMap<PlayerStatEnum, Integer>> gems, int maxGemSlots,
                          int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(templateId, displayableItem, tags, data, count, id, () -> {
            List<String> lore = new ArrayList<>();
            for (Map.Entry<PlayerStatEnum, RunicItemStat> entry : stats.entrySet()) {
                int finalValue = entry.getValue().getValue();
                if (finalValue == 0) continue;
                for (LinkedHashMap<PlayerStatEnum, Integer> gem : gems) {
                    if (gem.containsKey(entry.getKey())) {
                        finalValue += gem.get(entry.getKey());
                    }
                }
                lore.add(
                        entry.getKey().getChatColor()
                                + (finalValue < 0 ? "-" : "+")
                                + (finalValue != entry.getValue().getValue() ?
                                ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + entry.getValue().getValue() + ChatColor.RESET + "" + entry.getKey().getChatColor()
                                : "")
                                + entry.getValue().getValue()
                                + entry.getKey().getIcon()
                );
            }
            return new ItemLoreSection[] {
                    (maxGemSlots > 0 ?
                    new ItemLoreSection(new String[] {
                            ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level,
                            ChatColor.GRAY + "[" + ChatColor.WHITE + gems.size() + ChatColor.GRAY + "/" + ChatColor.WHITE + maxGemSlots + ChatColor.GRAY + "] Gems",
                    }) : new ItemLoreSection(new String[] {
                            ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level,
                    })),
                    new ItemLoreSection(new String[] {
                            ChatColor.RED + "" + health + "❤"
                    }),
                    new ItemLoreSection(lore),
                    new ItemLoreSection(new String[] {
                            rarity.getDisplay(),
                            ChatColor.GRAY + runicClass.getDisplay()
                    }),
            };
        });
        this.rarity = rarity;
        this.level = level;
        this.health = health;
        this.gems = gems;
        this.stats = stats;
        this.maxGemSlots = maxGemSlots;
        this.runicClass = runicClass;
    }

    public RunicItemArmor(RunicItemArmorTemplate template, int count, long id, LinkedHashMap<PlayerStatEnum, RunicItemStat> stats, List<LinkedHashMap<PlayerStatEnum, Integer>> gems) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getHealth(), stats, gems, template.getMaxGemSlots(),
                template.getLevel(), template.getRarity(), template.getRunicClass()
        );
    }

    public int getHealth() {
        return this.health;
    }

    public LinkedHashMap<PlayerStatEnum, RunicItemStat> getStats() {
        return this.stats;
    }

    public List<LinkedHashMap<PlayerStatEnum, Integer>> getGems() {
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
        for (PlayerStatEnum statType : this.stats.keySet()) {
            section.set(ItemManager.getInventoryPath() + "." + root + ".stats." + statType.getName(), this.stats.get(statType).getRollPercentage());
        }
        int count = 0;
        for (LinkedHashMap<PlayerStatEnum, Integer> gem : this.gems) {
            for (PlayerStatEnum statType : gem.keySet()) {
                section.set(ItemManager.getInventoryPath() + "." + root + ".gems." + count + "." + statType, gem.get(statType));
            }
            count++;
        }
    }

    @Override
    public ItemStack generateItem() {
        ItemStack item = super.generateItem();
        NBTItem nbtItem = new NBTItem(item, true);
        int count = 0;
        for (PlayerStatEnum statType : this.stats.keySet()) {
            nbtItem.setDouble("stat-" + count + "-" + statType.getName(), this.stats.get(statType).getRollPercentage());
            count++;
        }
        count = 0;
        for (LinkedHashMap<PlayerStatEnum, Integer> gem : this.gems) {
            int count2 = 0;
            for (PlayerStatEnum statType : this.stats.keySet()) {
                nbtItem.setInteger("gem-" + count + "-" + count2 + "-" + statType.getName(), gem.get(statType));
                count2++;
            }
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
        List<Pair<PlayerStatEnum, RunicItemStat>> statsList = new ArrayList<>(amountOfStats);
        for (int i = 0; i < amountOfStats; i++) {
            statsList.add(null);
        }
        for (String key : keys) {
            String[] split = key.split("-");
            if (split[0].equals("stat")) {
                Bukkit.broadcastMessage(Arrays.toString(split) + ", " + split.length);
                PlayerStatEnum statType = PlayerStatEnum.getFromName(split[2]);
                RunicItemStat stat = new RunicItemStat(template.getStats().get(statType), nbtItem.getDouble(key));
                statsList.set(Integer.parseInt(split[1]), new Pair<>(statType, stat));
            }
        }
        LinkedHashMap<PlayerStatEnum, RunicItemStat> stats = new LinkedHashMap<>();
        for (Pair<PlayerStatEnum, RunicItemStat> stat : statsList) {
            stats.put(stat.getKey(), stat.getValue());
        }
        int amountOfGems = 0;
        for (String key : keys) {
            if (key.startsWith("gem")) {
                amountOfGems++;
            }
        }
        List<List<Pair<PlayerStatEnum, Integer>>> gemsList = new ArrayList<>(amountOfGems);
        for (int i = 0; i < amountOfGems; i++) {
            gemsList.add(null);
        }
        for (String key : keys) {
            String[] split = key.split("-");
            if (split[0].equals("gem")) {;
                int gemNumber = Integer.parseInt(split[1]);
                PlayerStatEnum statType = PlayerStatEnum.getFromName(split[3]);
                if (gemsList.get(gemNumber) == null) {
                    int amountOfGemStats = 0;
                    for (String gemKey : keys) {
                        String[] splitGem = gemKey.split("-");
                        if (splitGem[0].equals("gem") && splitGem[1].equals(split[1])) {
                            amountOfGemStats++;
                        }
                    }
                    gemsList.set(gemNumber, new ArrayList<>(amountOfGemStats));
                }
                gemsList.get(gemNumber).set(Integer.parseInt(split[2]), new Pair<>(PlayerStatEnum.getFromName(split[3]), nbtItem.getInteger(key)));
            }
        }
        List<LinkedHashMap<PlayerStatEnum, Integer>> gems = new ArrayList<>();
        for (List<Pair<PlayerStatEnum, Integer>> gem : gemsList) {
            LinkedHashMap<PlayerStatEnum, Integer> newGem = new LinkedHashMap<>();
            for (Pair<PlayerStatEnum, Integer> gemStat : gem) {
                newGem.put(gemStat.getKey(), gemStat.getValue());
            }
            gems.add(newGem);
        }
        return new RunicItemArmor(template, item.getAmount(), nbtItem.getInteger("id"), stats, gems);
    }

}
