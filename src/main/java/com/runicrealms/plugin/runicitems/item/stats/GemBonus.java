package com.runicrealms.plugin.runicitems.item.stats;

import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.util.StatUtil;

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

    public GemBonus(Stat mainStat, int tier) {
        this.stats = StatUtil.generateGemBonuses(tier, mainStat);
        this.health = 0;
        this.mainStat = mainStat;
        this.tier = tier;
    }

    public int getHealth() {
        return this.health;
    }

    public Stat getMainStat() {
        return this.mainStat;
    }

    public LinkedHashMap<Stat, Integer> getStats() {
        return this.stats;
    }

    public int getTier() {
        return this.tier;
    }

    public boolean hasHealth() {
        return this.health != 0;
    }

}
