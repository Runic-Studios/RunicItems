package com.runicrealms.runicitems.item.stats;

import org.bukkit.ChatColor;

public enum RunicItemRarity {

    COMMON("common", ChatColor.GRAY, "Common"),
    UNCOMMON("uncommon", ChatColor.GREEN, "Uncommmon"),
    RARE("rare", ChatColor.AQUA, "Rare"),
    EPIC("epic", ChatColor.LIGHT_PURPLE, "Epic"),
    LEGENDARY("legendary", ChatColor.GOLD, "Legendary"),
    UNIQUE("unique", ChatColor.YELLOW, "Unique"),
    CRAFTED("crafted", ChatColor.WHITE, "Crafted");

    private final String identifier;
    private final ChatColor chatColor;
    private final String display;

    RunicItemRarity(String identifier, ChatColor chatColor, String display) {
        this.identifier = identifier;
        this.chatColor = chatColor;
        this.display = display;
    }

    public String getDisplay() {
        return this.chatColor + this.display;
    }

    public ChatColor getChatColor() {
        return this.chatColor;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public static RunicItemRarity getFromIdentifier(String identifier) {
        for (RunicItemRarity rarity : RunicItemRarity.values()) {
            if (rarity.getIdentifier().equalsIgnoreCase(identifier)) {
                return rarity;
            }
        }
        return null;
    }

}
