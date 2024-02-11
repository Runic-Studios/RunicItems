package com.runicrealms.plugin.runicitems.loot;

public class GenericLootHolder implements LootHolder {

    private final int minLevel;
    private final int maxLevel;

    public GenericLootHolder(int minLevel, int maxLevel) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    @Override
    public int getItemMinLevel() {
        return this.minLevel;
    }

    @Override
    public int getItemMaxLevel() {
        return this.maxLevel;
    }
}
