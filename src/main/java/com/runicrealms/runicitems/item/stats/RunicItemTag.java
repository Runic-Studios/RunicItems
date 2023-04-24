package com.runicrealms.runicitems.item.stats;

import org.bukkit.ChatColor;

public enum RunicItemTag {

    CONSUMABLE("consumable", ChatColor.GRAY + "Consumable"),
    FOOD("food", ChatColor.YELLOW + "Food"),
    POTION("potion", ChatColor.BLUE + "Potion"),
    SOULBOUND("soulbound", ChatColor.DARK_GRAY + "Soulbound"),
    UNTRADEABLE("untradeable", ChatColor.DARK_RED + "Untradeable"),
    QUEST_ITEM("quest-item", ChatColor.GOLD + "Quest Item");

    private final String identifier;
    private final String display;

    RunicItemTag(String identifier, String display) {
        this.identifier = identifier;
        this.display = display;
    }

    public static RunicItemTag getFromIdentifier(String identifier) {
        for (RunicItemTag tag : RunicItemTag.values()) {
            if (tag.getIdentifier().equalsIgnoreCase(identifier)) {
                return tag;
            }
        }
        return null;
    }

    public String getDisplay() {
        return this.display;
    }

    public String getIdentifier() {
        return identifier;
    }

}
