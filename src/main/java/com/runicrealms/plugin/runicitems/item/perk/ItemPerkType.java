package com.runicrealms.plugin.runicitems.item.perk;

public class ItemPerkType {

    private final String identifier;

    public ItemPerkType(String identifier) {
        this.identifier = identifier.toLowerCase();
    }

    public String getIdentifier() {
        return this.identifier;
    }

}
