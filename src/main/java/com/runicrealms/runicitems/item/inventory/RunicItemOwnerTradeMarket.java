package com.runicrealms.runicitems.item.inventory;

public class RunicItemOwnerTradeMarket implements RunicItemOwner {

    private String uuid;

    public RunicItemOwnerTradeMarket(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public RunicInventory getInventory() {
        return RunicInventory.TRADE_MARKET;
    }

    @Override
    public String getIdentifier() {
        return uuid;
    }
}
