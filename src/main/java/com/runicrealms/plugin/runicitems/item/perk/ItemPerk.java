package com.runicrealms.plugin.runicitems.item.perk;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemPerk itemPerk)) return false;
        return stacks == itemPerk.stacks && Objects.equals(type, itemPerk.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, stacks);
    }
    
}
