package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.ItemManager;
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
import javafx.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.Callable;

public class RunicItemWeapon extends RunicItem {

    protected final RunicItemStatRange damageRange;
    protected final LinkedHashMap<Stat, RunicItemStat> stats;
    protected final int level;
    protected final RunicItemRarity rarity;
    protected final RunicItemClass runicClass;

    public RunicItemWeapon(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                           RunicItemStatRange damageRange, LinkedHashMap<Stat, RunicItemStat> stats,
                           int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(templateId, displayableItem, tags, data, count, id, () -> {
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
        });
        this.damageRange = damageRange;
        this.stats = stats;
        this.level = level;
        this.rarity = rarity;
        this.runicClass = runicClass;
    }

    public RunicItemWeapon(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                           RunicItemStatRange damageRange, LinkedHashMap<Stat, RunicItemStat> stats,
                           int level, RunicItemRarity rarity, RunicItemClass runicClass, Callable<ItemLoreSection[]> loreSectionGenerator) {
        super(templateId, displayableItem, tags, data, count, id, loreSectionGenerator);
        this.damageRange = damageRange;
        this.stats = stats;
        this.level = level;
        this.rarity = rarity;
        this.runicClass = runicClass;
    }

    public RunicItemWeapon(RunicItemWeaponTemplate template, int count, long id, LinkedHashMap<Stat, RunicItemStat> stats) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getDamageRange(), stats,
                template.getLevel(), template.getRarity(), template.getRunicClass()
        );
    }

    public RunicItemStatRange getWeaponDamage() {
        return this.damageRange;
    }

    public LinkedHashMap<Stat, RunicItemStat> getStats() {
        return stats;
    }

    public int getLevel() {
        return this.level;
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

    public static RunicItemWeapon getFromItemStack(ItemStack item) {
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
            stats.put(stat.getKey(), stat.getValue());
        }
        return new RunicItemWeapon(template, item.getAmount(), nbtItem.getInteger("id"), stats);
    }

}
