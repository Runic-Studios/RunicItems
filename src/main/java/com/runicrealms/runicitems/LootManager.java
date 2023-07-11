package com.runicrealms.runicitems;

import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import com.runicrealms.runicitems.item.template.RunicItemArmorTemplate;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicitems.item.template.RunicItemWeaponTemplate;
import com.runicrealms.runicitems.item.template.RunicRarityLevelItemTemplate;
import com.runicrealms.runicitems.item.util.RunicItemClass;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LootManager {
    public static final List<RunicItemRarity> RARITY_DROP_TABLE_WEIGHTED;
    private static final List<RarityItemType> RARITY_ARMOR_WEAPON_DISTRIB_WEIGHTED;

    static {
        Map<RunicItemRarity, Integer> weights = new HashMap<>();
        weights.put(RunicItemRarity.COMMON, 20);
        weights.put(RunicItemRarity.UNCOMMON, 15);
        weights.put(RunicItemRarity.RARE, 4);
        weights.put(RunicItemRarity.EPIC, 1);

        RARITY_DROP_TABLE_WEIGHTED = new ArrayList<>(20 + 15 + 4 + 1);
        for (RunicItemRarity rarity : weights.keySet()) {
            for (int i = 0; i < weights.get(rarity); i++) {
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

    private static final Random random = ThreadLocalRandom.current();

    private LootManager() {

    }

    /**
     * A method used to easily get an item that meets the following conditions, or null if none meet the conditions
     *
     * @param range       the item range, default 0-60
     * @param rarities    the rarities allowed, default COMMON, UNCOMMON, RARE, EPIC
     * @param playerClass the usable classes of the item to pick randomly from, default is all classes
     * @param itemTypes   the type of item
     * @param lqm         where 1.0 is the default value, values below 1.0 cause your loot to favor more common items (you get less epics and rares, more commons etc) and above 1.0 favors rarer items
     * @return a future/promise for an item that meets the following conditions, or null if none meet the conditions
     */
    @NotNull
    public static CompletableFuture<RunicRarityLevelItemTemplate> getItem(@Nullable Pair<Integer, Integer> range, @Nullable Set<RunicItemRarity> rarities, @Nullable RunicItemClass playerClass, @Nullable Set<ItemType> itemTypes, @Nullable Float lqm) {
        return CompletableFuture.supplyAsync(() -> {
            int level = ThreadLocalRandom.current().nextInt(range != null && range.first < range.second ? range.second + 1 - range.first : 60 + 1);

            RunicItemRarity rarity = rarities == null || rarities.isEmpty() || rarities.stream().allMatch(r -> r == RunicItemRarity.COMMON || r == RunicItemRarity.UNCOMMON || r == RunicItemRarity.RARE || r == RunicItemRarity.EPIC) ? rollRarity() : rollRarity(rarities);

            return Stream.concat(itemTypes != null && !itemTypes.isEmpty() && (itemTypes.contains(ItemType.HELMET) || itemTypes.contains(ItemType.CHESTPLATE) || itemTypes.contains(ItemType.LEGGINGS) || itemTypes.contains(ItemType.BOOTS)) ? armorItems.get(level).get(rarity).stream() : Stream.empty(),
                            itemTypes != null && !itemTypes.isEmpty() && itemTypes.contains(ItemType.WEAPON) ? weaponItems.get(level).get(rarity).stream() : Stream.empty())
                    .filter(template -> {
                        if (playerClass == null || playerClass == RunicItemClass.ANY) {
                            return true;
                        }

                        if (template instanceof RunicItemWeaponTemplate weaponTemplate && weaponTemplate.getRunicClass() != playerClass) {
                            return false;
                        }

                        return !(template instanceof RunicItemArmorTemplate armorTemplate) || (armorTemplate.getRunicClass() == playerClass && (itemTypes == null || itemTypes.contains(ItemType.getItemType(armorTemplate))));
                    }).findAny().orElse(null); //lgm stuff not implemented
        }, executor -> Bukkit.getScheduler().runTaskAsynchronously(RunicItems.getInstance(), executor));
    }

    @NotNull
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
     *
     * @param min - minimum levels
     * @param max - maximum level
     * @return random item
     */
    public static RunicRarityLevelItemTemplate getRandomItemInRange(int min, int max) {
        int amount = 0;
        RunicItemRarity rarity = rollRarity();

        Map<Integer, Map<RunicItemRarity, List<RunicRarityLevelItemTemplate>>> rarityItems;
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

    /**
     * A method used to get a random rarity from the given rarity options (only valid options are COMMON, UNCOMMON, RARE, EPIC)
     *
     * @param rarities the given rarity options (only valid options are COMMON, UNCOMMON, RARE, EPIC)
     * @return a random rarity from the given rarity options
     */
    @NotNull
    private static RunicItemRarity rollRarity(@NotNull Set<RunicItemRarity> rarities) {
        Map<RunicItemRarity, Double> probabilityDistribution = new HashMap<>();
        probabilityDistribution.put(RunicItemRarity.EPIC, 1.0 / 40);
        probabilityDistribution.put(RunicItemRarity.RARE, 1.0 / 10);
        probabilityDistribution.put(RunicItemRarity.UNCOMMON, 3.0 / 8);
        probabilityDistribution.put(RunicItemRarity.COMMON, 1.0 / 2);

        for (Map.Entry<RunicItemRarity, Double> rarity : probabilityDistribution.entrySet()) {
            if (!probabilityDistribution.containsKey(rarity.getKey()) || rarities.contains(rarity.getKey())) {
                continue;
            }

            probabilityDistribution.remove(rarity.getKey());

            if (probabilityDistribution.size() == 0) {
                continue;
            }

            double prob = rarity.getValue() / probabilityDistribution.size();
            probabilityDistribution.forEach((itemRarity, blank) -> probabilityDistribution.put(itemRarity, probabilityDistribution.get(itemRarity) + prob));
        }

        if (probabilityDistribution.isEmpty()) {
            return rollRarity();
        }

        double random = Math.random();
        for (Map.Entry<RunicItemRarity, Double> rarity : probabilityDistribution.entrySet()) {
            if (random <= rarity.getValue()) {
                return rarity.getKey();
            }
        }

        return RunicItemRarity.COMMON;
    }

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

                    if (!rarityItems.containsKey(template.getLevel()))
                        rarityItems.put(template.getLevel(), new HashMap<>());
                    if (!rarityItems.get(template.getLevel()).containsKey(template.getRarity())) {
                        rarityItems.get(template.getLevel()).put(template.getRarity(), new LinkedList<>());
                    }
                    rarityItems.get(template.getLevel()).get(template.getRarity()).add(template);
                }
            }
        }
    }

    public enum ItemType {
        WEAPON,
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS;

        @Nullable
        public static ItemType getItemType(@NotNull String item) {
            try {
                return ItemType.valueOf(item.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        @NotNull
        public static ItemType getItemType(@NotNull RunicRarityLevelItemTemplate item) {
            if (item instanceof RunicItemWeaponTemplate) {
                return WEAPON;
            }

            if (!(item instanceof RunicItemArmorTemplate armorTemplate)) {
                throw new IllegalStateException("item " + item.getDisplayableItem().getDisplayName() + " not armor or weapon");
            }

            for (ItemType type : ItemType.values()) {
                if (item.getDisplayableItem().getMaterial().name().contains(type.name())) {
                    return type;
                }
            }

            throw new IllegalStateException("item " + item.getDisplayableItem().getDisplayName() + " is a type of armor that is not implemented in com.runicrealms.runicitems.LootManager.ItemType");
        }
    }

    private enum RarityItemType {
        WEAPON, ARMOR
    }
}
