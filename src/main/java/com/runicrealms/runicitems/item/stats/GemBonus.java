package com.runicrealms.runicitems.item.stats;

import com.runicrealms.runicitems.Stat;

import java.util.LinkedHashMap;

public class GemBonus {

    private final LinkedHashMap<Stat, Integer> stats;
    private int health;

    public GemBonus(LinkedHashMap<Stat, Integer> stats, int health) {
        this.stats = stats;
        this.health = health;
    }

    public LinkedHashMap<Stat, Integer> getStats() {
        return this.stats;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getHealth() {
        return this.health;
    }

    public boolean hasHealth() {
        return this.health != 0;
    }

}
