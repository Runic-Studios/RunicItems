package com.runicrealms.runicitems.item.stats;

import org.bukkit.ChatColor;

public enum RunicItemTag {

    SOULBOUND("soulbound", ChatColor.DARK_BLUE + "Soulbound"),
    UNTRADEABLE("untradeable", ChatColor.DARK_RED + "Untradeable"),
    QUEST_ITEM("quest-item", ChatColor.DARK_PURPLE + "Quest Item");

    private String identifier;
    private String display;

    RunicItemTag(String identifier, String display) {
        this.identifier = identifier;
        this.display = display;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDisplay() {
        return this.display;
    }

}
