package com.runicrealms.runicitems.item.stats;

import org.bukkit.ChatColor;

public enum RunicItemTag {

    SOULBOUND("soulbound", ChatColor.DARK_BLUE + "Soulbound"),
    UNTRADEABLE("untradeable", ChatColor.DARK_RED + "Untradeable"),
    QUEST_ITEM("quest-item", ChatColor.DARK_PURPLE + "Quest Item");

    private final String identifier;
    private final String display;

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

    public static RunicItemTag getFromIdentifier(String identifier) {
        for (RunicItemTag tag : RunicItemTag.values()) {
            if (tag.getIdentifier().equalsIgnoreCase(identifier)) {
                return tag;
            }
        }
        return null;
    }

}
