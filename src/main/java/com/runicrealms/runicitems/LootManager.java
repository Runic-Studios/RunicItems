package com.runicrealms.runicitems;

import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.template.RunicRarityLevelItemTemplate;

import java.util.*;

public class LootManager {

    public static final HashMap<RunicItemRarity, Integer> RARITY_DROP_TABLE_WEIGHTS;
    public static final List<RunicItemRarity> RARITY_DROP_TABLE_WEIGHTED;

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
    }
    private static final Map<Integer, Map<RunicItemRarity, List<RunicRarityLevelItemTemplate>>> rarityItems = new HashMap<>();
    private static final Random random = new Random();

    public static RunicItemRarity rollRarity() {
        return RARITY_DROP_TABLE_WEIGHTED.get(random.nextInt(RARITY_DROP_TABLE_WEIGHTED.size()));
    }

    public static void sortItems(Map<String, RunicItemTemplate> templates) {
        for (Map.Entry<String, RunicItemTemplate> entry : templates.entrySet()) {
            if (entry.getValue() instanceof RunicRarityLevelItemTemplate) {
                RunicRarityLevelItemTemplate template = (RunicRarityLevelItemTemplate) entry.getValue();
                if (!rarityItems.containsKey(template.getLevel())) {
                    rarityItems.put(template.getLevel(), new HashMap<>());
                }
                if (!rarityItems.get(template.getLevel()).containsKey(template.getRarity())) {
                    rarityItems.get(template.getLevel()).put(template.getRarity(), new LinkedList<>());
                }
                rarityItems.get(template.getLevel()).get(template.getRarity()).add(template);
            }
        }
    }

    public static List<RunicRarityLevelItemTemplate> getTemplatesInLevel(int level, RunicItemRarity rarity) {
        if (!rarityItems.containsKey(level)) return new ArrayList<>();
        return rarityItems.get(level).get(rarity);
    }

    /**
     * Gets a random item in level range, INCLUSIVE.
     * @param min - minimum level
     * @param max - maximum level
     * @return random item
     */
    public static RunicRarityLevelItemTemplate getRandomItemInRange(int min, int max) {
        int amount = 0;
        RunicItemRarity rarity = rollRarity();
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

}
