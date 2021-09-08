package com.runicrealms.runicitems.player;

import com.runicrealms.runicitems.Stat;

import java.util.LinkedHashMap;

public class AddedArmorStats {

    private final LinkedHashMap<Stat, Integer> stats;
    private int health;

    public AddedArmorStats(LinkedHashMap<Stat, Integer> stats, int health) {
        this.stats = stats;
        this.health = health;
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

}
