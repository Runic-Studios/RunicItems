package com.runicrealms.runicitems;

import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.template.RunicRarityLevelItemTemplate;

import java.util.*;

public class LootManager {

    private static final Map<Integer, List<RunicRarityLevelItemTemplate>> rarityItems = new HashMap<>();
    private static final Random random = new Random();

    public static void sortItems(Map<String, RunicItemTemplate> templates) {
        for (Map.Entry<String, RunicItemTemplate> entry : templates.entrySet()) {
            if (entry.getValue() instanceof RunicRarityLevelItemTemplate) {
                RunicRarityLevelItemTemplate template = (RunicRarityLevelItemTemplate) entry.getValue();
                if (!rarityItems.containsKey(template.getLevel())) rarityItems.put(template.getLevel(), new ArrayList<>());
                rarityItems.get(template.getLevel()).add(template);
            }
        }
    }

    public static List<RunicRarityLevelItemTemplate> getTemplatesInLevel(int level) {
        if (!rarityItems.containsKey(level)) return new ArrayList<>();
        return rarityItems.get(level);
    }

    /**
     * Gets a random item in level range, INCLUSIVE.
     * @param min - minimum level
     * @param max - maximum level
     * @return random item
     */
    public static RunicRarityLevelItemTemplate getRandomItemInRange(int min, int max) {
        int amount = 0;
        for (int i = min; i <= max; i++) {
            if (rarityItems.containsKey(i)) amount += rarityItems.get(i).size();
        }
        int itemNumber = random.nextInt(amount);
        for (int i = min; i <= max; i++) {
            if (!rarityItems.containsKey(i)) continue;
            if (itemNumber >= rarityItems.get(i).size()) {
                itemNumber -= rarityItems.get(i).size();
            } else {
                return rarityItems.get(i).get(itemNumber);
            }
        }
        return null; // Never happen, just read my math its good trust me
    }

}
