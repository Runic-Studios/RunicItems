package com.runicrealms.plugin.runicitems.player;

public interface StatsModifier {

    /**
     * Gets the AddedStats that we will combine with our current stats.
     */
    AddedStats getChanges(AddedStats currentStats);

}
