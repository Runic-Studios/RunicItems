package com.runicrealms.runicitems.item.inventory;

public class RunicItemOwnerGuildBank implements RunicItemOwner {

    private String guildPrefix;

    public RunicItemOwnerGuildBank(String guildPrefix) {
        this.guildPrefix = guildPrefix;
    }

    @Override
    public RunicInventory getInventory() {
        return RunicInventory.GUILD_BANK;
    }

    @Override
    public String getIdentifier() {
        return this.guildPrefix;
    }

}
