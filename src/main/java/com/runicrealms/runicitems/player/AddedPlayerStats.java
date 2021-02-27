package com.runicrealms.runicitems.player;

import com.runicrealms.runicitems.item.stats.RunicArtifactAbility;
import com.runicrealms.runicitems.item.stats.RunicItemStatType;

import java.util.Map;

public class AddedPlayerStats {

    private Map<RunicItemStatType, Integer> addedStats;
    private int addedHealth;
    private RunicArtifactAbility ability;

    public AddedPlayerStats(Map<RunicItemStatType, Integer> addedStats, int addedHealth, RunicArtifactAbility ability) {
        this.addedStats = addedStats;
        this.addedHealth = addedHealth;
        this.ability = ability;
    }

    public Map<RunicItemStatType, Integer> getAddedStats() {
        return addedStats;
    }

    public int getAddedHealth() {
        return addedHealth;
    }

    public RunicArtifactAbility getAbility() {
        return ability;
    }

    public void setAddedStats(Map<RunicItemStatType, Integer> addedStats) {
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
