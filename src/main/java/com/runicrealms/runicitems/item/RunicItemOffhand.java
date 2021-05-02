package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.database.Data;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.plugin.player.stat.PlayerStatEnum;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemOffhandTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import de.tr7zw.nbtapi.NBTItem;
import javafx.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RunicItemOffhand extends RunicItem {

    private final LinkedHashMap<PlayerStatEnum, RunicItemStat> stats;
    private final int level;
    private final RunicItemRarity rarity;

    public RunicItemOffhand(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                            LinkedHashMap<PlayerStatEnum, RunicItemStat> stats,
                            int level, RunicItemRarity rarity) {
        super(templateId, displayableItem, tags, data, count, id, () -> {
            List<String> lore = new ArrayList<String>();
            for (Map.Entry<PlayerStatEnum, RunicItemStat> entry : stats.entrySet()) {
                lore.add(
                        entry.getKey().getChatColor()
                                + (entry.getValue().getValue() < 0 ? "-" : "+")
                                + entry.getValue().getValue()
                                + entry.getKey().getName()
                );
            }
            return new ItemLoreSection[] {
                    new ItemLoreSection(new String[] {
                            rarity.getDisplay(),
                            ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level,
                    }),
                    new ItemLoreSection(lore)
            };
        });
        this.stats = stats;
        this.level = level;
        this.rarity = rarity;
    }

    public RunicItemOffhand(RunicItemOffhandTemplate template, int count, long id, LinkedHashMap<PlayerStatEnum, RunicItemStat> stats) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                stats,
                template.getLevel(), template.getRarity()
        );
    }

    public LinkedHashMap<PlayerStatEnum, RunicItemStat> getStats() {
        return this.stats;
    }

    public int getLevel() {
        return this.level;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

    @Override
    public void addToData(Data section, String root) {
        super.addToData(section, root);
        String dataPrefix = root.equals("") ? "" : root + ".";
        for (PlayerStatEnum statType : this.stats.keySet()) {
            section.set(dataPrefix + "stats." + statType.getName(), this.stats.get(statType).getRollPercentage());
        }
    }

    @Override
    public ItemStack generateItem() {
        ItemStack item = super.generateItem();
        NBTItem nbtItem = new NBTItem(item, true);
        int count = 0;
        for (PlayerStatEnum statType : this.stats.keySet()) {
            nbtItem.setFloat("stat-" + count + "-" + statType.getName(), this.stats.get(statType).getRollPercentage());
            count++;
        }
        return item;
    }

    public static RunicItemOffhand getFromItemStack(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemOffhandTemplate)) throw new IllegalArgumentException("ItemStack is not an offhand item!");
        RunicItemOffhandTemplate template = (RunicItemOffhandTemplate) uncastedTemplate;
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
                PlayerStatEnum statType = PlayerStatEnum.getFromName(split[2]);
                RunicItemStat stat = new RunicItemStat(template.getStats().get(statType), nbtItem.getFloat(key));
                statsList.set(Integer.parseInt(split[1]), new Pair<>(statType, stat));
            }
        }
        LinkedHashMap<PlayerStatEnum, RunicItemStat> stats = new LinkedHashMap<>();
        for (Pair<PlayerStatEnum, RunicItemStat> stat : statsList) {
            stats.put(stat.getKey(), stat.getValue());
        }
        return new RunicItemOffhand(template, item.getAmount(), nbtItem.getInteger("id"), stats);
    }

}
