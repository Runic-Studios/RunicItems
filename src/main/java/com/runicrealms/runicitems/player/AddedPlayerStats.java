package com.runicrealms.runicitems.player;

import com.runicrealms.plugin.player.stat.PlayerStatEnum;
import com.runicrealms.runicitems.item.stats.RunicArtifactAbility;

import java.util.Map;

public class AddedPlayerStats {

    private Map<PlayerStatEnum, Integer> addedStats;
    private int addedHealth;
    private RunicArtifactAbility ability;

    public AddedPlayerStats(Map<PlayerStatEnum, Integer> addedStats, int addedHealth, RunicArtifactAbility ability) {
        this.addedStats = addedStats;
        this.addedHealth = addedHealth;
        this.ability = ability;
    }

    public Map<PlayerStatEnum, Integer> getAddedStats() {
        return addedStats;
    }

    public int getAddedHealth() {
        return addedHealth;
    }

    public RunicArtifactAbility getAbility() {
        return ability;
    }

    public void setAddedStats(Map<PlayerStatEnum, Integer> addedStats) {
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
