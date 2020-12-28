package com.runicrealms.runicitems.item.inventory;

public enum RunicInventory {

    PLAYER_INVENTORY("player-inventory"),
    PLAYER_BANK("player-bank"),
    GUILD_BANK("guild-bank"),
    TRADE_MARKET("trade-market"),
    DROPPED("dropped");

    private String identifier;

    RunicInventory(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public static RunicInventory getFromIdentifier(String identifier) {
        for (RunicInventory inventory : RunicInventory.values()) {
            if (inventory.getIdentifier().equalsIgnoreCase(identifier)) {
                return inventory;
            }
        }
        return null;
    }

}
