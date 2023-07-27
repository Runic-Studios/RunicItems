package com.runicrealms.plugin.runicitems.player;

import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.item.stats.RunicArtifactAbility;

import java.util.Map;

public class AddedPlayerStats {

    private Map<Stat, Integer> addedStats;
    private int addedHealth;
    private RunicArtifactAbility ability;

    public AddedPlayerStats(Map<Stat, Integer> addedStats, int addedHealth, RunicArtifactAbility ability) {
        this.addedStats = addedStats;
        this.addedHealth = addedHealth;
        this.ability = ability;
    }

    public Map<Stat, Integer> getAddedStats() {
        return addedStats;
    }

    public int getAddedHealth() {
        return addedHealth;
    }

    public RunicArtifactAbility getAbility() {
        return ability;
    }

    public void setAddedStats(Map<Stat, Integer> addedStats) {
        this.addedStats = addedStats;
    }

    public void setAddedHealth(int addedHealth) {
        this.addedHealth = addedHealth;
    }

    public void setAbility(RunicArtifactAbility ability) {
        this.ability = ability;
    }

    public boolean hasAbility() {
        return this.ability != null;
    }

}
