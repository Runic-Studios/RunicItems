package com.runicrealms.runicitems.util;

import com.runicrealms.runicitems.Stat;
import javafx.util.Pair;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StatUtil {

    // Maps gem tier to a collection of options including the main stat value (pair key) and the sub stat values (pair entry)
    public static final Map<Integer, List<Pair<Integer, int[]>>> GEM_STAT_OPTIONS;

    static {
        GEM_STAT_OPTIONS = new HashMap<>();

        List<Pair<Integer, int[]>> tierOne = new ArrayList<>(1);
        tierOne.add(new Pair<>(1, new int[0]));
        GEM_STAT_OPTIONS.put(1, tierOne);

        List<Pair<Integer, int[]>> tierTwo = new ArrayList<>(1);
        tierTwo.add(new Pair<>(2, new int[0]));
        GEM_STAT_OPTIONS.put(2, tierTwo);

        List<Pair<Integer, int[]>> tierThree = new ArrayList<>(2);
        tierThree.add(new Pair<>(3, new int[0]));
        tierThree.add(new Pair<>(2, new int[] {1, 1}));
        GEM_STAT_OPTIONS.put(3, tierThree);

        List<Pair<Integer, int[]>> tierFour = new ArrayList<>(3);
        tierFour.add(new Pair<>(4, new int[0]));
        tierFour.add(new Pair<>(3, new int[] {2}));
        tierFour.add(new Pair<>(3, new int[] {1, 1, 1}));
        GEM_STAT_OPTIONS.put(4, tierFour);

        List<Pair<Integer, int[]>> tierFive = new ArrayList<>(4);
        tierFive.add(new Pair<>(5, new int[0]));
        tierFive.add(new Pair<>(4, new int[] {3}));
        tierFive.add(new Pair<>(4, new int[] {2, 2}));
        tierFive.add(new Pair<>(4, new int[] {1, 1, 1, 1}));
        GEM_STAT_OPTIONS.put(5, tierFive);
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
        for (int subStatBonus : selectedOption.getValue()) {
            Stat selectedSubStat = Stat.PLAYER_STATS[random.nextInt(Stat.PLAYER_STATS.length)];;
            while (selectedSubStat == mainStat || stats.containsKey(selectedSubStat)) selectedSubStat = Stat.PLAYER_STATS[random.nextInt(Stat.PLAYER_STATS.length)];
            stats.put(selectedSubStat, subStatBonus);
        }
        return sortStatMap(stats);
    }

}
