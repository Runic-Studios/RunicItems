package com.runicrealms.plugin.runicitems.player;

import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerk;
import com.runicrealms.plugin.runicitems.item.stats.RunicArtifactAbility;

import java.util.Map;
import java.util.Set;

public class AddedPlayerStats {

    private Map<Stat, Integer> addedStats;
    private Set<ItemPerk> itemPerks;
    private int addedHealth;
    private RunicArtifactAbility ability;

    public AddedPlayerStats(Map<Stat, Integer> addedStats, Set<ItemPerk> itemPerks, int addedHealth, RunicArtifactAbility ability) {
        this.addedStats = addedStats;
        this.itemPerks = itemPerks;
        this.addedHealth = addedHealth;
        this.ability = ability;
    }

    public Map<Stat, Integer> getAddedStats() {
        return addedStats;
    }

    public void setAddedStats(Map<Stat, Integer> addedStats) {
        this.addedStats = addedStats;
    }

    public int getAddedHealth() {
        return addedHealth;
    }

    public void setAddedHealth(int addedHealth) {
        this.addedHealth = addedHealth;
    }

    public RunicArtifactAbility getAbility() {
        return ability;
    }

    public void setAbility(RunicArtifactAbility ability) {
        this.ability = ability;
    }

    public boolean hasAbility() {
        return this.ability != null;
    }

}
