package com.runicrealms.runicitems.item.stats;

import com.runicrealms.runicitems.util.ItemIcons;
import org.bukkit.ChatColor;

public enum RunicItemStatType {

    INTELLIGENCE("intelligence", "INT", ChatColor.DARK_AQUA, ItemIcons.WAND_ICON),
    DEXTERITY("dexterity", "DEX", ChatColor.DARK_GREEN, ItemIcons.SHIELD_ICON),
    STRENGTH("strength", "STR", ChatColor.DARK_RED, ItemIcons.SWORD_ICON),
    VITALITY("vitality", "VIT", ChatColor.RED, ItemIcons.HEART_ICON),
    WISDOM("wisdom", "WIS", ChatColor.GREEN, ItemIcons.STAR_ICON),
    CRITICAL("critical", "CRIT", ChatColor.YELLOW, ItemIcons.SWORD_ICON + ItemIcons.SWORD_ICON),
    DODGE("dodge", "DODGE", ChatColor.WHITE, ItemIcons.DODGE_ICON),
    ATTACK_SPEED("attack-speed", "ATK SPD", ChatColor.GRAY, ItemIcons.ATTACK_SPEED_ICON);

    private final String identifier;
    private final String display;
    private final ChatColor color;
    private final String suffix;

    RunicItemStatType(String identifier, String display, ChatColor color, String suffix) {
        this.identifier = identifier;
        this.display = display;
        this.color = color;
        this.suffix = suffix;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getDisplay() {
        return this.display;
    }

    public ChatColor getColor() {
        return this.color;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public static RunicItemStatType getFromIdentifier(String identifier) {
        for (RunicItemStatType stat : RunicItemStatType.values()) {
            if (stat.getIdentifier().equalsIgnoreCase(identifier)) {
                return stat;
            }
        }
        return null;
    }

}
