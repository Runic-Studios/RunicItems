package com.runicrealms.runicitems.util;

import com.runicrealms.runicitems.Stat;
import javafx.util.Pair;

import java.util.*;

public class StatUtil {

    // Maps gem tier to a collection of options including the main stat value (pair key) and the sub stat values (pair entry)
    public static final Map<Integer, List<Pair<Integer, int[]>>> GEM_STAT_OPTIONS;
    // Maps a gem tier to the number of gem slots (slots) it consumes
    public static final Map<Integer, Integer> GEM_TIER_SLOTS;

    static {
        GEM_STAT_OPTIONS = new HashMap<>();

        List<Pair<Integer, int[]>> tierOne = new ArrayList<>(1);
        tierOne.add(new Pair<>(1, new int[0]));
        GEM_STAT_OPTIONS.put(1, tierOne);

        List<Pair<Integer, int[]>> tierTwo = new ArrayList<>(1);
        tierTwo.add(new Pair<>(3, new int[0]));
        GEM_STAT_OPTIONS.put(2, tierTwo);

        List<Pair<Integer, int[]>> tierThree = new ArrayList<>(2);
        tierThree.add(new Pair<>(4, new int[0]));
        tierThree.add(new Pair<>(3, new int[] {1, 1}));
        GEM_STAT_OPTIONS.put(3, tierThree);

        List<Pair<Integer, int[]>> tierFour = new ArrayList<>(3);
        tierFour.add(new Pair<>(7, new int[0]));
        tierFour.add(new Pair<>(6, new int[] {2}));
        tierFour.add(new Pair<>(6, new int[] {1, 1, 1}));
        GEM_STAT_OPTIONS.put(4, tierFour);

        List<Pair<Integer, int[]>> tierFive = new ArrayList<>(4);
        tierFive.add(new Pair<>(8, new int[0]));
        tierFive.add(new Pair<>(7, new int[] {2}));
        tierFive.add(new Pair<>(6, new int[] {2, 2}));
        tierFive.add(new Pair<>(6, new int[] {1, 1, 1, 1}));
        GEM_STAT_OPTIONS.put(5, tierFive);

        GEM_TIER_SLOTS = new HashMap<>();
        GEM_TIER_SLOTS.put(1, 1);
        GEM_TIER_SLOTS.put(2, 2);
        GEM_TIER_SLOTS.put(3, 2);
        GEM_TIER_SLOTS.put(4, 3);
        GEM_TIER_SLOTS.put(5, 3);
    }

    private static final Random random = new Random();


    /**
     * Sorts a Map of Stat and generic parameter T by Stat enum declaration order.
     * (So according to the declarations of enum values in Stat).
     * @param stats Map of stats.
     * @param <T> Generic parameter T representing what we are mapping the stats to.
     * @return LinkedHashMap that is ordered (linked) according to how we stored.
     */
    public static <T> LinkedHashMap<Stat, T> sortStatMap(Map<Stat, T> stats) {
        LinkedHashMap<Stat, T> sorted = new LinkedHashMap<>();
        stats.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach((entry) -> sorted.put(entry.getKey(), entry.getValue()));
        return sorted;
    }

    /**
     * Generates randomized bonuses that a gem will give based upon its tier and main stat.
     * Tier should be a number between 1 and 5, <b>0 isn't a tier</b>.
     * @param tier Tier of the gem
     * @param mainStat Main stat of the gem
     * @return Sorted map of the stats
     */
    public static LinkedHashMap<Stat, Integer> generateGemBonuses(int tier, Stat mainStat) {
        if (tier < 0 || tier > GEM_STAT_OPTIONS.size()) throw new IllegalArgumentException("Tier " + tier + " does not exist for gem bonuses.");
        Map<Stat, Integer> stats = new HashMap<>();
        List<Pair<Integer, int[]>> tierOptions = GEM_STAT_OPTIONS.get(tier);
        int index = random.nextInt(tierOptions.size());
        Pair<Integer, int[]> selectedOption = tierOptions.get(index);
        stats.put(mainStat, selectedOption.getKey());
        List<Stat> statsToChoose = new ArrayList<>(Arrays.asList(Stat.PLAYER_STATS));
        statsToChoose.remove(mainStat);
        for (int subStatBonus : selectedOption.getValue()) {
            int randomSubStatIndex = random.nextInt(statsToChoose.size());
            Stat selectedSubStat = statsToChoose.get(randomSubStatIndex);
            stats.put(selectedSubStat, subStatBonus);
            statsToChoose.remove(randomSubStatIndex);
        }
        return sortStatMap(stats);
    }

    /**
     * Gets the number of gem slots (slots) that the specified gem will consume.
     * @param tier Gem tier
     * @return Number of slots
     */
    public static int getGemSlots(int tier) {
        return GEM_TIER_SLOTS.get(tier);
    }

}
