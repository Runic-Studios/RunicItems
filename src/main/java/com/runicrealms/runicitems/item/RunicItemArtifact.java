package com.runicrealms.runicitems.item;

import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.runicitems.RunicItems;
import com.runicrealms.runicitems.Stat;
import com.runicrealms.runicitems.TemplateManager;
import com.runicrealms.runicitems.item.stats.RunicArtifactAbility;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.stats.RunicItemStat;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.item.template.RunicItemArtifactTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.util.DisplayableItem;
import com.runicrealms.runicitems.item.util.ItemLoreSection;
import com.runicrealms.runicitems.item.util.RunicItemClass;
import com.runicrealms.runicitems.weaponskin.WeaponSkin;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RunicItemArtifact extends RunicItemWeapon {

    private final RunicArtifactAbility ability;
    private final RunicItemRarity rarity;

    public RunicItemArtifact(String templateId, DisplayableItem displayableItem, List<RunicItemTag> tags, Map<String, String> data, int count, long id,
                             RunicArtifactAbility ability, RunicItemStatRange damageRange, LinkedHashMap<Stat, RunicItemStat> stats,
                             int level, RunicItemRarity rarity, RunicItemClass runicClass, WeaponSkin activeSkin) {
        super(templateId, displayableItem, tags, data, count, id, damageRange, stats, level, rarity, runicClass, activeSkin);
        this.ability = ability;
        this.rarity = rarity;
    }

    public RunicItemArtifact(RunicItemArtifactTemplate template, int count, long id, LinkedHashMap<Stat, RunicItemStat> stats, WeaponSkin activeSkin) {
        this(
                template.getId(), template.getDisplayableItem(), template.getTags(), template.getData(), count, id,
                template.getAbility(), template.getDamageRange(), stats,
                template.getLevel(), template.getRarity(), template.getRunicClass(), activeSkin
        );
    }

    public static RunicItemArtifact getFromItemStack(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(item);
        RunicItemTemplate uncastedTemplate = TemplateManager.getTemplateFromId(nbtItem.getString("template-id"));
        if (!(uncastedTemplate instanceof RunicItemArtifactTemplate template))
            throw new IllegalArgumentException("ItemStack is not an artifact item!");
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
            stats.put(stat.first, stat.second);
        }
        WeaponSkin skin = null;
        if (nbtItem.hasNBTData() && nbtItem.hasKey("weapon-skin")) {
            skin = RunicItems.getWeaponSkinAPI().getWeaponSkin(nbtItem.getString("weapon-skin"));
        }
        return new RunicItemArtifact(template, item.getAmount(), nbtItem.getInteger("id"), stats, skin);
    }

//    @Override
//    public ItemStack generateItem() {
//        ItemStack item = super.generateItem();
//        NBTItem nbtItem = new NBTItem(item, true);
//        nbtItem.setString("ability-id", this.ability.getIdentifier());
//        nbtItem.setString("ability-trigger", this.ability.getTrigger().getIdentifier());
//        return item;
//    }

    @Override
    protected ItemLoreSection[] generateLore() {
        ItemLoreSection[] sections = new ItemLoreSection[3 + (stats.size() > 0 ? 1 : 0)]; // 4
        sections[0] = new ItemLoreSection(new String[]{ChatColor.GRAY + "Lv. Min " + ChatColor.WHITE + "" + level});
        sections[1] = new ItemLoreSection(new String[]{ChatColor.RED + "" + damageRange.getMin() + "-" + damageRange.getMax() + " DMG"});
//        List<String> formattedDescription = new ArrayList<>(Collections.singleton
//                (
//                        ability.getTrigger().getDisplay() + " " + ChatColor.RESET +
//                                ChatColor.GREEN + ability.getAbilityName()
//                ));
//        formattedDescription.addAll(ChatUtils.formattedText(ability.getDescription()));
//        String[] artifactLore = new String[formattedDescription.size()];
//        formattedDescription.toArray(artifactLore);
//        sections[2] = new ItemLoreSection(artifactLore);
        List<String> lore = new LinkedList<>();
        for (Map.Entry<Stat, RunicItemStat> entry : stats.entrySet()) {
            lore.add(
                    entry.getKey().getChatColor()
                            + (entry.getValue().getValue() < 0 ? "-" : "+")
                            + entry.getValue().getValue()
                            + entry.getKey().getIcon()
            );
        }
        sections[2] = new ItemLoreSection(new String[]{rarity.getDisplay(), ChatColor.GRAY + runicClass.getDisplay()});
        if (stats.size() > 0) {
            sections[3] = new ItemLoreSection(lore);
        }
        return sections;
    }

    public RunicItemRarity getRarity() {
        return this.rarity;
    }

    public RunicArtifactAbility getAbility() {
        return this.ability;
    }

}
