package com.runicrealms.runicitems.item.stats;

import com.runicrealms.runicitems.Stat;

import java.util.LinkedHashMap;

public class GemBonus {

    private final LinkedHashMap<Stat, Integer> stats;
    private final int health;
    private final Stat mainStat;
    private final int tier;

    public GemBonus(LinkedHashMap<Stat, Integer> stats, int health, Stat mainStat, int tier) {
        this.stats = stats;
        this.health = health;
        this.mainStat = mainStat;
        this.tier = tier;
    }

    public LinkedHashMap<Stat, Integer> getStats() {
        return this.stats;
    }

    public int getHealth() {
        return this.health;
    }

    public boolean hasHealth() {
        return this.health != 0;
    }

    public Stat getMainStat() {
        return this.mainStat;
    }

    public int getTier() {
        return this.tier;
    }

}
