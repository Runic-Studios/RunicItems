package com.runicrealms.plugin.runicitems.item.perk;

public class ItemPerk {

    private final ItemPerkType type;
    private final int stacks;

    public ItemPerk(ItemPerkType type, int stacks) {
        this.type = type;
        this.stacks = stacks;
    }

    public ItemPerkType getType() {
        return this.type;
    }

    public int getStacks() {
        return this.stacks;
    }

}
