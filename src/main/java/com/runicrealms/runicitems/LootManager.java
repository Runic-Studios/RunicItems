package com.runicrealms.runicitems;

import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.runicitems.item.template.RunicRarityLevelItemTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LootManager {

    public static final HashMap<RunicItemRarity, Integer> RARITY_DROP_TABLE_WEIGHTS;
    public static final List<RunicItemRarity> RARITY_DROP_TABLE_WEIGHTED;

    private static final List<RarityItemType> RARITY_ARMOR_WEAPON_DISTRIB_WEIGHTED;

    static {
        RARITY_DROP_TABLE_WEIGHTS = new HashMap<>();
        RARITY_DROP_TABLE_WEIGHTS.put(RunicItemRarity.COMMON, 20);
        RARITY_DROP_TABLE_WEIGHTS.put(RunicItemRarity.UNCOMMON, 15);
        RARITY_DROP_TABLE_WEIGHTS.put(RunicItemRarity.RARE, 4);
        RARITY_DROP_TABLE_WEIGHTS.put(RunicItemRarity.EPIC, 1);

        RARITY_DROP_TABLE_WEIGHTED = new ArrayList<>(20 + 15 + 4 + 1);
        for (RunicItemRarity rarity : RARITY_DROP_TABLE_WEIGHTS.keySet()) {
            for (int i = 0; i < RARITY_DROP_TABLE_WEIGHTS.get(rarity); i++) {
                RARITY_DROP_TABLE_WEIGHTED.add(rarity);
            }
        }

        RARITY_ARMOR_WEAPON_DISTRIB_WEIGHTED = new ArrayList<>(5);
        for (int i = 0; i < 4; i++) RARITY_ARMOR_WEAPON_DISTRIB_WEIGHTED.add(RarityItemType.ARMOR);
        RARITY_ARMOR_WEAPON_DISTRIB_WEIGHTED.add(RarityItemType.WEAPON);
    }
    // Maps level to a mapping between rarities and a list of available templates
    private static final Map<Integer, Map<RunicItemRarity, List<RunicRarityLevelItemTemplate>>> armorItems = new HashMap<>();
    // This includes artifacts because artifact instanceof weapon
    private static final Map<Integer, Map<RunicItemRarity, List<RunicRarityLevelItemTemplate>>> weaponItems = new HashMap<>();

    private static final Random random = new Random();

    public static RunicItemRarity rollRarity() {
        return RARITY_DROP_TABLE_WEIGHTED.get(random.nextInt(RARITY_DROP_TABLE_WEIGHTED.size()));
    }

    public static void sortItems(Map<String, RunicItemTemplate> templates) {
        for (Map.Entry<String, RunicItemTemplate> entry : templates.entrySet()) {
            if (entry.getValue() instanceof RunicRarityLevelItemTemplate) {
                if (entry.getKey().startsWith("script")) {
                    RunicRarityLevelItemTemplate template = (RunicRarityLevelItemTemplate) entry.getValue();
                    Map<Integer, Map<RunicItemRarity, List<RunicRarityLevelItemTemplate>>> rarityItems = null;
                    if (entry.getValue() instanceof RunicItemArmorTemplate) {
                        rarityItems = armorItems;
                    } else if (entry.getValue() instanceof RunicItemWeaponTemplate) {
                        rarityItems = weaponItems;
                    }
                    if (rarityItems == null) continue;

                    if (!rarityItems.containsKey(template.getLevel())) rarityItems.put(template.getLevel(), new HashMap<>());
                    if (!rarityItems.get(template.getLevel()).containsKey(template.getRarity())) {
                        rarityItems.get(template.getLevel()).put(template.getRarity(), new LinkedList<>());
                    }
                    rarityItems.get(template.getLevel()).get(template.getRarity()).add(template);
                }
            }
        }
    }

    public static List<RunicRarityLevelItemTemplate> getTemplatesInLevel(int level, RunicItemRarity rarity) {
        if (!armorItems.containsKey(level) && !weaponItems.containsKey(level)) return new ArrayList<>();
        if (!armorItems.containsKey(level)) return weaponItems.get(level).get(rarity);
        if (!weaponItems.containsKey(level)) return armorItems.get(level).get(rarity);
        return Stream.concat(
                armorItems.get(level).get(rarity).stream(),
                weaponItems.get(level).get(rarity).stream()).collect(Collectors.toList());
    }

    /**
     * Gets a random item in level range, INCLUSIVE.
     * @param min - minimum levels
     * @param max - maximum level
     * @return random item
     */
    public static RunicRarityLevelItemTemplate getRandomItemInRange(int min, int max) {
        int amount = 0;
        RunicItemRarity rarity = rollRarity();

        Map<Integer, Map<RunicItemRarity, List<RunicRarityLevelItemTemplate>>> rarityItems = null;
        switch (RARITY_ARMOR_WEAPON_DISTRIB_WEIGHTED.get(random.nextInt(RARITY_ARMOR_WEAPON_DISTRIB_WEIGHTED.size()))) {
            case ARMOR:
                rarityItems = armorItems;
                break;
            case WEAPON:
                rarityItems = weaponItems;
                break;
            default:
                // Kotlin removes need for default because if you use a "when" case on an enum,
                // you can apply the "sealed" keyword to make it exhaustive (values are checked during compile time, not run time)
                throw new RuntimeException("Could not find script generated loot for levels " + min + "-" + max + " rarity " + rarity.getIdentifier().toUpperCase() + ".");
        }

        for (int i = min; i <= max; i++) {
            if (rarityItems.containsKey(i)) {
                if (rarityItems.get(i).containsKey(rarity)) amount += rarityItems.get(i).get(rarity).size();
            }
        }
        int itemNumber = random.nextInt(amount);
        for (int i = min; i <= max; i++) {
            if (!rarityItems.containsKey(i)) continue;
            if (!rarityItems.get(i).containsKey(rarity)) continue;
            if (itemNumber >= rarityItems.get(i).get(rarity).size()) {
                itemNumber -= rarityItems.get(i).get(rarity).size();
            } else {
                return rarityItems.get(i).get(rarity).get(itemNumber);
            }
        }
        throw new RuntimeException("Could not find script generated loot for levels " + min + "-" + max + " rarity " + rarity.getIdentifier().toUpperCase() + ".");
    }

    private enum RarityItemType {

        WEAPON, ARMOR;

    }

}
