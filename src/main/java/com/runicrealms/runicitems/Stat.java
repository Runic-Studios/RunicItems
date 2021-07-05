package com.runicrealms.runicitems;

import org.bukkit.ChatColor;

public enum Stat {

    /*
    Player stats
     */
    DEXTERITY("dexterity", "Dexterity", "DEX", ChatColor.YELLOW, "✦", "Deal more ranged damage and gain movement speed!"),
    INTELLIGENCE("intelligence", "Intelligence", "INT", ChatColor.DARK_AQUA, "ʔ", "Deal more spell damage and gain more max mana!"),
    STRENGTH("strength", "Strength", "STR", ChatColor.RED, "⚔", "Deal more melee weapon damage!"),
    VITALITY("vitality", "Vitality", "VIT", ChatColor.WHITE, "■", "Gain damage reduction and health regen!"),
    WISDOM("wisdom", "Wisdom", "WIS", ChatColor.GREEN, "✸", "Gain more spell healing and mana regen!"),
    ATTACK_SPEED("attack-speed", "Attack Speed", "ATK SPD", ChatColor.GRAY, "", "Determines the swing speed of your weapon!"),
    /*
    Item-exclusive stats
     */
    CRIT("critical", "Crit", "CRIT", ChatColor.YELLOW, " CRIT", "Chance to deal a critical strike!"),
    DODGE("dodge", "Dodge", "DODGE", ChatColor.WHITE, " DODGE","Chance to dodge the damage of an attack!");

    /*
    Combat multipliers
     */
    private static final double MOVEMENT_SPEED_MULT = 1.0;
    private static final double RANGED_DMG_MULT = 0.9;
    private static final double MAGIC_DMG_MULT = 1.0;
    private static final double MAX_MANA_MULT = 1.0;
    private static final double MELEE_DMG_MULT = 1.1;
    private static final double DAMAGE_REDUCTION_MULT = 1.0;
    private static final double HEALTH_REGEN_MULT = 1.0;
    private static final double SPELL_HEALING_MULT = 1.0;
    private static final double MANA_REGEN_MULT = 1.0;
    /*
    Damage caps
     */
    private static final double DAMAGE_REDUCTION_CAP = 40;
    /*
    Enum fields
     */
    private final String identifier;
    private final String name;
    private final String prefix;
    private final ChatColor chatColor;
    private final String icon;
    private final String description;

    Stat(String identifier, String name, String prefix, ChatColor chatColor, String icon, String description) {
        this.identifier = identifier;
        this.name = name;
        this.prefix = prefix;
        this.chatColor = chatColor;
        this.icon = icon;
        this.description = description;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    /*
    Multipliers returned as value / 100, so 1.0 returns .01. Better for damage calcualtion.
     */
    public static double getRangedDmgMult() {
        return RANGED_DMG_MULT / 100;
    }

    public static double getMovementSpeedMult() {
        return MOVEMENT_SPEED_MULT / 100;
    }

    public static double getMagicDmgMult() {
        return MAGIC_DMG_MULT / 100;
    }

    public static double getMaxManaMult() {
        return MAX_MANA_MULT / 100;
    }

    public static double getMeleeDmgMult() {
        return MELEE_DMG_MULT / 100;
    }

    public static double getDamageReductionMult() {
        return DAMAGE_REDUCTION_MULT / 100;
    }

    public static double getHealthRegenMult() {
        return HEALTH_REGEN_MULT / 100;
    }

    public static double getSpellHealingMult() {
        return SPELL_HEALING_MULT / 100;
    }

    public static double getManaRegenMult() {
        return MANA_REGEN_MULT / 100;
    }

    public static double getDamageReductionCap() {
        return DAMAGE_REDUCTION_CAP;
    }

    /**
     * Returns the enum value of a stat from its string
     * @param identifier of stat (not case sensitive)
     * @return enum of stat
     */
    public static Stat getFromIdentifier(String identifier) {
        for (Stat stat : Stat.values()) {
            if (stat.getIdentifier().equalsIgnoreCase(identifier)) {
                return stat;
            }
        }
        return null;
    }

    public static Stat getFromName(String name) {
        for (Stat stat : Stat.values()) {
            if (stat.getName().equalsIgnoreCase(name)) {
                return stat;
            }
        }
        return null;
    }

}
