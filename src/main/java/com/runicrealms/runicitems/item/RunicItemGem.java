package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.ItemManager;
import com.runicrealms.runicitems.Stat;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.GemBonus;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemGemTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.util.StatUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RunicItemGem extends RunicItem {

    private final LinkedHashMap<Stat, Integer> stats;
    private final int health;
    private final int tier;

    public RunicItemGem(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data,
                        int count, long id, LinkedHashMap<Stat, Integer> stats, int health, int tier) {
        super(templateId, displayableItem, tags, data, count, id, () -> {
            List<String> lore = new ArrayList<>();

            for (Stat stat : stats.keySet()) {
                int value = stats.get(stat);
                if (value == 0) continue;;
                lore.add(stat.getChatColor()
                        + (value < 0 ? "-" : "+")
                        + value
                        + stat.getIcon());
            }

            return new ItemLoreSection[] {
                    new ItemLoreSection(new String[] {ChatColor.GRAY + "Drag and click on armor to apply this gem."}),
                    new ItemLoreSection(lore)
            };

        });
        this.stats = stats;
        this.health = health;
        this.tier = tier;
    }

    public RunicItemGem(RunicItemGemTemplate template, int count, long id, LinkedHashMap<Stat, Integer> stats, int health) {
        this(template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id, stats, health, template.getTier());
    }

    public LinkedHashMap<Stat, Integer> getStats() {
        return this.stats;
    }

    public int getHealth() {
        return this.health;
    }

    public boolean hasHealth() {
        return this.health != 0;
    }

    public GemBonus generateGemBonus() {
        return new GemBonus(this.stats, this.health);
    }

    @Override
    public ItemStack generateItem() {
        ItemStack item = super.generateItem();
        NBTItem nbtItem = new NBTItem(item, true);
        for (Stat stat : this.stats.keySet()) {
            nbtItem.setInteger("gem-" + stat.getIdentifier(), this.stats.get(stat));
        }
        if (this.health != 0) nbtItem.setInteger("gem-health", this.health);
        return item;
    }

    @Override
    public void addToData(Data section, String root) {
        super.addToData(section, root);
        for (Stat statType : this.stats.keySet()) {
            section.set(ItemManager.getInventoryPath() + "." + root + ".gem-stats." + statType.getIdentifier(), this.stats.get(statType));
        }
    }

    public static RunicItemGem getFromItemStack(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemGemTemplate)) throw new IllegalArgumentException("ItemStack is not a gem item!");
        RunicItemGemTemplate template = (RunicItemGemTemplate) uncastedTemplate;
        Set<String> keys = nbtItem.getKeys();
        Map<Stat, Integer> stats = new HashMap<>();
        int health = 0;
        for (String key : keys) {
            if (key.startsWith("gem-")) {
                String statString = key.substring(4);
                if (statString.equalsIgnoreCase("health")) {
                    health = nbtItem.getInteger(key);
                } else {
                    Stat stat = Stat.getFromIdentifier(statString);
                    if (stat == null) continue;
                    stats.put(stat, nbtItem.getInteger(key));
                }
            }
        }
        return new RunicItemGem(template, item.getAmount(), nbtItem.getInteger("id"), StatUtil.sortStatMap(stats), health);
    }

}
