package com.runicrealms.runicitems.util;

import com.runicrealms.runicitems.Stat;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatUtil {

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

}
