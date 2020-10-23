package com.runicrealms.runicitems.item.stats;

import org.bukkit.ChatColor;

public enum RunicItemRarity {

    COMMON("common", ChatColor.GRAY + "Common"),
    UNCOMMON("uncommon", ChatColor.GREEN + "Uncommmon"),
    RARE("rare", ChatColor.AQUA + "Rare"),
    EPIC("epic", ChatColor.LIGHT_PURPLE + "Epic"),
    CRAFTED("crafted", ChatColor.WHITE + "Crafted");

    private String identifier;
    private String display;

    RunicItemRarity(String identifier, String display) {
        this.identifier = identifier;
        this.display = display;
    }

    public String getDisplay() {
        return this.display;
    }

    public String getIdentifier() {
        return this.identifier;
    }

}
