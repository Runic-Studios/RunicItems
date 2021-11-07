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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RunicItemGem extends RunicItem {

    private final GemBonus bonus;

    public RunicItemGem(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data,
                        int count, long id, GemBonus bonus) {
        super(templateId, displayableItem, tags, data, count, id, () -> {
            List<String> lore = new ArrayList<>();

            for (Stat stat : bonus.getStats().keySet()) {
                int value = bonus.getStats().get(stat);
                if (value == 0) continue;;
                lore.add(stat.getChatColor()
                        + (value < 0 ? "-" : "+")
                        + value
                        + stat.getIcon());
            }

            return new ItemLoreSection[] {
                    new ItemLoreSection(new String[] {ChatColor.GRAY + "Req Slots " + ChatColor.WHITE + StatUtil.getGemSlots(bonus.getTier())}),
                    new ItemLoreSection(new String[] {
                            ChatColor.GRAY + "" + ChatColor.ITALIC + "Drag and click on armor",
                            ChatColor.GRAY + "" + ChatColor.ITALIC + "to apply this gem!"
                    }),
                    new ItemLoreSection(lore)
            };

        });
        this.bonus = bonus;
    }

    public RunicItemGem(RunicItemGemTemplate template, int count, long id, GemBonus bonus) {
        this(template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id, bonus);
    }

    public GemBonus getBonus() {
        return this.bonus;
    }
    @Override
    public ItemStack generateItem() {
        ItemStack item = super.generateItem();
        NBTItem nbtItem = new NBTItem(item, true);
        for (Stat stat : this.bonus.getStats().keySet()) {
            nbtItem.setInteger("gem-" + stat.getIdentifier(), this.bonus.getStats().get(stat));
        }
        if (this.bonus.getHealth() != 0) nbtItem.setInteger("gem-health", this.bonus.getHealth());
        nbtItem.setString("gem-main", this.bonus.getMainStat().getIdentifier());
        nbtItem.setInteger("gem-tier", this.bonus.getTier());
        return item;
    }

    @Override
    public void addToData(Data section, String root) {
        super.addToData(section, root);
        for (Stat statType : this.bonus.getStats().keySet()) {
            section.set(ItemManager.getInventoryPath() + "." + root + ".gem-stats." + statType.getIdentifier(), this.bonus.getStats().get(statType));
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
        Stat mainStat = null;
        int tier = 1;
        for (String key : keys) {
            if (key.startsWith("gem-")) {
                String statString = key.substring(4);
                if (statString.equalsIgnoreCase("health")) {
                    health = nbtItem.getInteger(key);
                } else if (statString.equalsIgnoreCase("main")) {
                    mainStat = Stat.getFromIdentifier(nbtItem.getString(key));
                } else if (statString.equalsIgnoreCase("tier")) {
                    tier = nbtItem.getInteger(key);
                } else {
                    Stat stat = Stat.getFromIdentifier(statString);
                    if (stat == null) continue;
                    stats.put(stat, nbtItem.getInteger(key));
                }
            }
        }
        return new RunicItemGem(template, item.getAmount(), nbtItem.getInteger("id"), new GemBonus(StatUtil.sortStatMap(stats), health, mainStat, tier));
    }

}