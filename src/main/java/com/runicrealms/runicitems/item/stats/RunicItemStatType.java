package com.runicrealms.runicitems.item.stats;

import com.runicrealms.runicitems.util.ItemIcons;
import org.bukkit.ChatColor;

public enum RunicItemStatType {

    HEALTH("health", ChatColor.RED, ItemIcons.HEART_ICON),
    MANA("mana", ChatColor.DARK_AQUA, ItemIcons.MANA_ICON),
    MANA_REGEN("mana-regen", ChatColor.DARK_AQUA, ItemIcons.MANA_ICON + "/t"),
    SPELL_DAMAGE("spell-damage", ChatColor.YELLOW, ItemIcons.SPELL_ICON),
    WEAPON_DAMAGE("weapon-damage", ChatColor.YELLOW, ItemIcons.ATTACK_ICON),
    HEALTH_REGEN("health-regen", ChatColor.RED, ItemIcons.HEART_ICON + "/t"),
    HEALING("healing", ChatColor.DARK_RED, ItemIcons.SPELL_ICON + ItemIcons.HEART_ICON),
    SHIELD("shield", ChatColor.GRAY, ItemIcons.SHIELD_ICON),
    AGILITY("agility", ChatColor.WHITE, ItemIcons.AGILITY_ICON),
    CRITICAL("critical", ChatColor.YELLOW, ItemIcons.CRITICAL_ICON),
    DODGE("dodge", ChatColor.DARK_PURPLE, ItemIcons.DODGE_ICON),
    ATTACK_SPEED("attack-speed", ChatColor.GOLD, ItemIcons.ATTACK_ICON);

    private String identifier;
    private ChatColor color;
    private String suffix;

    RunicItemStatType(String identifier, ChatColor color, String suffix) {
        this.identifier = identifier;
        this.color = color;
        this.suffix = suffix;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public ChatColor getColor() {
        return this.color;
    }

    public String getSuffix() {
        return this.suffix;
    }

}
