package com.runicrealms.runicitems.item.inventory;

public class RunicItemOwnerPlayerInventory implements RunicItemOwner {

    private String uuid;

    public RunicItemOwnerPlayerInventory(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public RunicInventory getInventory() {
        return RunicInventory.PLAYER_INVENTORY;
    }

    @Override
    public String getIdentifier() {
        return this.uuid;
    }

}