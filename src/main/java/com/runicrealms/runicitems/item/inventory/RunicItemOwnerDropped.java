package com.runicrealms.runicitems.item.inventory;

public class RunicItemOwnerDropped implements RunicItemOwner {

    private String uuid;

    public RunicItemOwnerDropped(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public RunicInventory getInventory() {
        return RunicInventory.DROPPED;
    }

    @Override
    public String getIdentifier() {
        return this.uuid;
    }
}
