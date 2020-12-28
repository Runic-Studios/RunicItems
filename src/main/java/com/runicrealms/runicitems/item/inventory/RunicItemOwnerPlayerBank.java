package com.runicrealms.runicitems.item.inventory;

public class RunicItemOwnerPlayerBank implements RunicItemOwner {

    private String uuid;

    public RunicItemOwnerPlayerBank(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public RunicInventory getInventory() {
        return RunicInventory.PLAYER_BANK;
    }

    @Override
    public String getIdentifier() {
        return uuid;
    }

}
