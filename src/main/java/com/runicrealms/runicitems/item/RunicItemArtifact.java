package com.runicrealms.runicitems.item;

import com.runicrealms.runicitems.Stat;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicArtifactAbility;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemArtifactTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.RunicItemClass;
import de.tr7zw.nbtapi.NBTItem;
import javafx.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RunicItemArtifact extends RunicItemWeapon {

    private final RunicArtifactAbility ability;

    public RunicItemArtifact(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                             RunicArtifactAbility ability, RunicItemStatRange damageRange, LinkedHashMap<Stat, RunicItemStat> stats,
                             int level, RunicItemRarity rarity, RunicItemClass runicClass) {
        super(templateId, displayableItem, tags, data, count, id, damageRange, stats, level, rarity, runicClass, () -> {
            ItemLoreSection[] sections = new ItemLoreSection[3 + (stats.size() > 0 ? 1 : 0)];
            sections[0] = new ItemLoreSection(new String[] {
                    ChatColor.GRAY + "Req Class " + ChatColor.WHITE + runicClass.getDisplay(),
                    ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level,
                    rarity.getDisplay()
            });
            sections[1] = new ItemLoreSection(new String[] {
                    ChatColor.RED + "+ " + damageRange.getMin() + "-" + damageRange.getMax() + Stat.STRENGTH.getIcon()
            });
            sections[2] = new ItemLoreSection(new String[] {
                    ability.getTrigger().getDisplay() + " " + ChatColor.RESET + "" + ChatColor.GREEN + ability.getAbilityName(),
                    ChatColor.translateAlternateColorCodes('&', ability.getDescription())
            });
            List<String> lore = new ArrayList<>();
            for (Map.Entry<Stat, RunicItemStat> entry : stats.entrySet()) {
                lore.add(
                        entry.getKey().getChatColor()
                                + (entry.getValue().getValue() < 0 ? "-" : "+")
                                + entry.getValue().getValue()
                                + entry.getKey().getIcon()
                );
            }
            if (stats.size() > 0) {
                sections[3] = new ItemLoreSection(lore);
            }
            return sections;
        });
        this.ability = ability;
    }

    public RunicItemArtifact(RunicItemArtifactTemplate template, int count, long id, LinkedHashMap<Stat, RunicItemStat> stats) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getAbility(), template.getDamageRange(), stats,
                template.getLevel(), template.getRarity(), template.getRunicClass()
        );
    }

    public RunicArtifactAbility getAbility() {
        return this.ability;
    }

    @Override
    public ItemStack generateItem() {
        ItemStack item = super.generateItem();
        NBTItem nbtItem = new NBTItem(item, true);
        nbtItem.setString("ability-id", this.ability.getIdentifier());
        nbtItem.setString("ability-trigger", this.ability.getTrigger().getIdentifier());
        return item;
    }

    public static RunicItemArtifact getFromItemStack(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemArtifactTemplate)) throw new IllegalArgumentException("ItemStack is not an artifact item!");
        RunicItemArtifactTemplate template = (RunicItemArtifactTemplate) uncastedTemplate;
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
        return new RunicItemArtifact(template, item.getAmount(), nbtItem.getInteger("id"), stats);
    }

}
