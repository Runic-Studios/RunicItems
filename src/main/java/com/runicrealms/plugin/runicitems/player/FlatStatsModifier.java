package com.runicrealms.plugin.runicitems.player;

import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerk;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class FlatStatsModifier implements StatsModifier {

    private final AddedStats stats;

    // Pass null for no item perks
    public FlatStatsModifier(Map<Stat, Integer> stats, @Nullable Set<ItemPerk> itemPerks, int health) {
        this.stats = new AddedStats(stats, itemPerks, health);
    }

    @Override
    public AddedStats getChanges(AddedStats currentStats) {
        return this.stats;
    }
}
