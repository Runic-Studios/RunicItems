package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.ItemNbtUtils;
import com.runicrealms.runicitems.item.util.RunicItemClass;
import com.runicrealms.runicitems.util.ItemIcons;
import javafx.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RunicItemWeapon extends RunicItem {

    private final RunicItemStatRange damageRange;
    private final LinkedHashMap<RunicItemStatType, RunicItemStat> stats;
    private final int level;
    private final RunicItemRarity rarity;
    private final RunicItemClass runicClass;

    public RunicItemWeapon(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                           RunicItemStatRange damageRange, LinkedHashMap<RunicItemStatType, RunicItemStat> stats,
                           int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(templateId, displayableItem, tags, data, count, id, () -> {
            ItemLoreSection[] sections = new ItemLoreSection[2 + (stats.size() > 0 ? 1 : 0)];
            sections[0] = new ItemLoreSection(new String[]{
                    ChatColor.GRAY + "Required Class: " + ChatColor.WHITE + runicClass.getDisplay(),
                    ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level,
                    rarity.getDisplay()
            });
            sections[1] = new ItemLoreSection(new String[]{
                    ChatColor.RED + "+ " + damageRange.getMin() + "-" + damageRange.getMax() + ItemIcons.SWORD_ICON
            });
            List<String> lore = new ArrayList<String>();
            for (Map.Entry<RunicItemStatType, RunicItemStat> entry : stats.entrySet()) {
                lore.add(
                        entry.getKey().getColor()
                                + (entry.getValue().getRoll() < 0 ? "-" : "+")
                                + entry.getValue().getRoll()
                                + entry.getKey().getSuffix()
                );
            }
            if (stats.size() > 0) {
                sections[2] = new ItemLoreSection(lore);
            }
            return sections;
        });
        this.damageRange = damageRange;
        this.stats = stats;
        this.level = level;
        this.rarity = rarity;
        this.runicClass = runicClass;
    }

    public RunicItemWeapon(RunicItemWeaponTemplate template, int count, long id, LinkedHashMap<RunicItemStatType, RunicItemStat> stats) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getDamageRange(), stats,
                template.getLevel(), template.getRarity(), template.getRunicClass()
        );
    }

    public RunicItemStatRange getWeaponDamage() {
        return this.damageRange;
    }

    public LinkedHashMap<RunicItemStatType, RunicItemStat> getStats() {
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
    public void addToData(Data section) {
        super.addToData(section);
        for (RunicItemStatType statType : this.stats.keySet()) {
            section.set("stats." + statType.getIdentifier(), this.stats.get(statType).getRollPercentage());
        }
    }

    @Override
    public ItemStack generateItem() {
        ItemStack item = super.generateItem();
        int count = 0;
        for (RunicItemStatType statType : this.stats.keySet()) {
            ItemNbtUtils.setNbt(item, "stat-" + count + "-" + statType.getIdentifier(), this.stats.get(statType).getRollPercentage());
            count++;
        }
        return item;
    }

    public static RunicItemWeapon getFromItemStack(ItemStack item) {
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(ItemNbtUtils.getNbtString(item, "template-id"));
        if (!(uncastedTemplate instanceof RunicItemWeaponTemplate)) throw new IllegalArgumentException("ItemStack is not a weapon item!");
        RunicItemWeaponTemplate template = (RunicItemWeaponTemplate) uncastedTemplate;
        Set<String> keys = ItemNbtUtils.getKeys(item);
        int amountOfStats = 0;
        for (String key : keys) {
            if (key.startsWith("stat")) {
                amountOfStats++;
            }
        }
        List<Pair<RunicItemStatType, RunicItemStat>> statsList = new ArrayList<>(amountOfStats);
        for (String key : keys) {
            String[] split = key.split("-");
            if (split[0].equals("stat")) {
                RunicItemStatType statType = RunicItemStatType.getFromIdentifier(split[2]);
                RunicItemStat stat = new RunicItemStat(template.getStats().get(statType), ItemNbtUtils.getNbtFloat(item, key));
                statsList.set(Integer.parseInt(split[1]), new Pair<>(statType, stat));
            }
        }
        LinkedHashMap<RunicItemStatType, RunicItemStat> stats = new LinkedHashMap<>();
        for (Pair<RunicItemStatType, RunicItemStat> stat : statsList) {
            stats.put(stat.getKey(), stat.getValue());
        }
        return new RunicItemWeapon(template, item.getAmount(), ItemNbtUtils.getNbtInteger(item, "id"), stats);
    }

}
