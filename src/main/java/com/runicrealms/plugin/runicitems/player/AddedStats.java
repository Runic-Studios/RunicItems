package com.runicrealms.plugin.runicitems.player;

import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerk;
import com.runicrealms.plugin.runicitems.item.perk.ItemPerkType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AddedStats {

    private final Map<Stat, Integer> addedStats;
    private @Nullable Set<ItemPerk> itemPerks;
    private int addedHealth;

    public AddedStats(Map<Stat, Integer> addedStats, @Nullable Set<ItemPerk> itemPerks, int addedHealth) {
        this.addedStats = addedStats;
        this.itemPerks = itemPerks;
        this.addedHealth = addedHealth;
    }

    public Map<Stat, Integer> getAddedStats() {
        return addedStats;
    }

    public int getAddedHealth() {
        return addedHealth;
    }

    public @Nullable Set<ItemPerk> getItemPerks() {
        return this.itemPerks;
    }

    public boolean hasItemPerks() {
        return this.itemPerks != null && this.itemPerks.size() > 0;
    }

    /**
     * Adds the stats of another AddedStats object to the stats of this object
     */
    public void combine(AddedStats moreStats) {
        this.addedHealth += moreStats.addedHealth;
        for (Stat stat : moreStats.addedStats.keySet()) {
            this.addedStats.put(stat, this.addedStats.getOrDefault(stat, 0) + moreStats.addedStats.get(stat));
        }
        if (this.itemPerks != null || moreStats.itemPerks != null) {
            Map<ItemPerkType, Integer> perks = new HashMap<>();
            if (this.itemPerks != null) {
                for (ItemPerk perk : this.itemPerks) {
                    perks.put(perk.getType(), perks.getOrDefault(perk.getType(), 0) + perk.getStacks());
                }
            }
            if (moreStats.itemPerks != null) {
                for (ItemPerk perk : moreStats.itemPerks) {
                    perks.put(perk.getType(), perks.getOrDefault(perk.getType(), 0) + perk.getStacks());
                }
            }
            if (this.itemPerks != null) {
                this.itemPerks.clear();
            } else {
                this.itemPerks = new HashSet<>();
            }
            for (ItemPerkType perkType : perks.keySet()) {
                this.itemPerks.add(new ItemPerk(perkType, perks.get(perkType)));
            }
        }
    }

}
