package com.runicrealms.plugin.runicitems.item.perk;

public class ItemPerkType {

    private final String identifier;
    private final int maxStacks;

    public ItemPerkType(String identifier, int maxStacks) {
        this.identifier = identifier.toLowerCase();
        this.maxStacks = maxStacks;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public int getMaxStacks() {
        return this.maxStacks;
    }

}
