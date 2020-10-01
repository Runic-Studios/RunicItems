package com.runicrealms.runicitems.item.stats;

import org.bukkit.ChatColor;

public enum RunicItemRarity {

    COMMON(ChatColor.GRAY + "Common"),
    UNCOMMON(ChatColor.GREEN + "Uncommmon"),
    RARE(ChatColor.AQUA + "Rare"),
    EPIC(ChatColor.LIGHT_PURPLE + "Epic"),
    CRAFTED(ChatColor.WHITE + "Crafted");

    private String display;

    RunicItemRarity(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return this.display;
    }

}
